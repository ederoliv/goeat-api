package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.AuthResponseDTO;
import br.com.ederoliv.goeat_api.dto.address.AddressRequestDTO;
import br.com.ederoliv.goeat_api.dto.address.AddressResponseDTO;
import br.com.ederoliv.goeat_api.dto.order.OrderDTO;
import br.com.ederoliv.goeat_api.dto.order.OrderResponseDTO;
import br.com.ederoliv.goeat_api.dto.order.OrderStatusDTO;
import br.com.ederoliv.goeat_api.dto.partner.PartnerLoginRequestDTO;
import br.com.ederoliv.goeat_api.dto.partner.PartnerRequestDTO;
import br.com.ederoliv.goeat_api.dto.partner.PartnerResponseDTO;
import br.com.ederoliv.goeat_api.dto.partner.PartnerWithCategoriesResponseDTO; // NOVO IMPORT
import br.com.ederoliv.goeat_api.entities.Partner;
import br.com.ederoliv.goeat_api.services.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/partners")
public class PartnerController {

    private final OrderService orderService;
    private final AddressService addressService;
    private final PartnerService partnerService;
    private final AuthenticationService authenticationService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getPartnerById(@PathVariable UUID id) {
        PartnerResponseDTO response = partnerService.getPartnerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PartnerResponseDTO>> getAllPartners() {
        return ResponseEntity.ok(partnerService.listAllPartners());
    }

    /**
     * Lista parceiros por categoria de restaurante (endpoint público)
     */
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<?> getPartnersByCategory(@PathVariable Long categoryId) {
        try {
            List<PartnerResponseDTO> partners = partnerService.getPartnersByCategory(categoryId);
            return ResponseEntity.ok(partners);
        } catch (Exception e) {
            log.error("Erro ao buscar parceiros por categoria {}", categoryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar parceiros por categoria: " + e.getMessage());
        }
    }

    /**
     * Lista parceiros por nome da categoria de restaurante (endpoint público)
     */
    @GetMapping("/by-category-name/{categoryName}")
    public ResponseEntity<?> getPartnersByCategoryName(@PathVariable String categoryName) {
        try {
            List<PartnerResponseDTO> partners = partnerService.getPartnersByCategoryName(categoryName);
            return ResponseEntity.ok(partners);
        } catch (Exception e) {
            log.error("Erro ao buscar parceiros por categoria {}", categoryName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar parceiros por categoria: " + e.getMessage());
        }
    }

    /**
     * Lista parceiros com suas categorias por categoria específica (endpoint público)
     */
    @GetMapping("/with-categories/by-category/{categoryId}")
    public ResponseEntity<?> getPartnersWithCategoriesByCategory(@PathVariable Long categoryId) {
        try {
            List<PartnerWithCategoriesResponseDTO> partners =
                    partnerService.getPartnersWithCategoriesByCategory(categoryId);
            return ResponseEntity.ok(partners);
        } catch (Exception e) {
            log.error("Erro ao buscar parceiros com categorias por categoria {}", categoryId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar parceiros com categorias: " + e.getMessage());
        }
    }

    /**
     * Lista todos os parceiros que possuem pelo menos uma categoria (endpoint público)
     */
    @GetMapping("/with-categories")
    public ResponseEntity<?> getPartnersWithCategories() {
        try {
            List<PartnerWithCategoriesResponseDTO> partners =
                    partnerService.getPartnersWithCategories();
            return ResponseEntity.ok(partners);
        } catch (Exception e) {
            log.error("Erro ao buscar parceiros com categorias", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar parceiros com categorias: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody PartnerLoginRequestDTO request) {
        try {
            // Tenta autenticar com o serviço de autenticação
            String token = authenticationService.authenticate(request.email(), request.password());

            // Se a autenticação for bem-sucedida, busca os detalhes do parceiro
            Partner partner = partnerService.findByEmail(request.email())
                    .orElseThrow(() -> new RuntimeException("Parceiro não encontrado"));

            // Constrói a resposta com o token JWT e os detalhes do parceiro
            AuthResponseDTO response = new AuthResponseDTO(
                    token,
                    partner.getName(),
                    partner.getId(),
                    "ROLE_PARTNER"
            );

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciais inválidas");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar login: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody PartnerRequestDTO request) {
        try {
            PartnerResponseDTO savedPartner = partnerService.registerPartner(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPartner);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao registrar parceiro: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/orders")
    public ResponseEntity<?> orderPartner(@PathVariable UUID id, @RequestBody OrderDTO orderDTO) {
        OrderResponseDTO responseDTO = orderService.createOrder(orderDTO, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/{id}/orders")
    public ResponseEntity<?> getOrderPartner(@PathVariable UUID id) {
        List<OrderResponseDTO> responseDTOList = orderService.getAllOrdersByPartnerId(id);
        return ResponseEntity.ok(responseDTOList);
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/{id}/orders/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable UUID id, @PathVariable Long orderId) {
        OrderResponseDTO orderResponseDTO = orderService.getOrderById(orderId);

        if (orderResponseDTO == null) {
            return ResponseEntity.badRequest().body("Erro ao buscar o parceiro");
        } else {
            return ResponseEntity.ok(orderResponseDTO);
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @PutMapping("/{id}/orders")
    public ResponseEntity<?> updateOrderPartner(@PathVariable UUID id, @RequestBody OrderStatusDTO orderStatusDTO) {
        return ResponseEntity.ok().body(orderService.updateOrderStatus(orderStatusDTO.id(), orderStatusDTO.status()));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @PostMapping("/address")
    public ResponseEntity<?> setAddress(@RequestBody AddressRequestDTO request, Authentication authentication) {
        try {
            // Extrai o partnerId do token JWT
            String partnerIdStr = ((Jwt) authentication.getPrincipal()).getClaim("partnerId");
            UUID partnerId = UUID.fromString(partnerIdStr);

            // Chama o serviço com o request original e o partnerId do token
            AddressResponseDTO response = addressService.registerPartnerAddress(request, partnerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao registrar endereço: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/address")
    public ResponseEntity<?> getFullAddress(@PathVariable UUID id) {
        try {
            String formattedAddress = addressService.getFullAddress(id);
            if (formattedAddress == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Endereço não encontrado para o parceiro");
            }
            return ResponseEntity.ok(formattedAddress);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar endereço: " + e.getMessage());
        }
    }
}