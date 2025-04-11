package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.AuthResponseDTO;
import br.com.ederoliv.goeat_api.dto.order.OrderDTO;
import br.com.ederoliv.goeat_api.dto.order.OrderResponseDTO;
import br.com.ederoliv.goeat_api.dto.partner.PartnerLoginRequestDTO;
import br.com.ederoliv.goeat_api.dto.partner.PartnerRequestDTO;
import br.com.ederoliv.goeat_api.dto.partner.PartnerResponseDTO;
import br.com.ederoliv.goeat_api.entities.Partner;
import br.com.ederoliv.goeat_api.services.AuthenticationService;
import br.com.ederoliv.goeat_api.services.OrderService;
import br.com.ederoliv.goeat_api.services.PartnerService;
import br.com.ederoliv.goeat_api.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/partners")
public class PartnerController {

    private final PartnerService partnerService;
    private final ProductService productService;
    private final OrderService orderService;
    private final AuthenticationService authenticationService;

    @GetMapping("/{id}")
    public ResponseEntity<PartnerResponseDTO> getPartnerById(@PathVariable UUID id) {
        PartnerResponseDTO response = partnerService.getPartnerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PartnerResponseDTO>> getAllPartners() {
        return ResponseEntity.ok(partnerService.listAllPartners());
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<?> getAllProductsByPartnerId(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(productService.listAllProductsByMenuId(id));
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

    @GetMapping("/{id}/orders")
    public ResponseEntity<?> getOrderPartner(@PathVariable UUID id) {
        List<OrderResponseDTO> responseDTOList = orderService.getAllOrdersByPartnerId(id);
        return ResponseEntity.ok(responseDTOList);
    }
}