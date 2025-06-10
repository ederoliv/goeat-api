package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.order.OrderResponseDTO;
import br.com.ederoliv.goeat_api.dto.orderItemDTO.OrderItemResponseDTO;
import br.com.ederoliv.goeat_api.services.AuthenticationUtilsService;
import br.com.ederoliv.goeat_api.services.OrderItemService;
import br.com.ederoliv.goeat_api.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/orders")
public class OrderController {

    private final OrderItemService orderItemService;
    private final OrderService orderService;
    private final AuthenticationUtilsService authenticationUtilsService;

    @PreAuthorize( "hasAnyAuthority('SCOPE_ROLE_CLIENT', 'SCOPE_ROLE_PARTNER')")
    @GetMapping("/{orderId}/items")
    public ResponseEntity<?> getOrderItems(@PathVariable Long orderId) {
        List<OrderItemResponseDTO> items = orderItemService.getOrderItems(orderId);
        return ResponseEntity.ok(items);
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_CLIENT')")
    @GetMapping("/client")
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        try {
            // Usar o serviço utilitário para extrair o clientId
            UUID clientId = authenticationUtilsService.getClientIdFromAuthentication(authentication);
            if (clientId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Não foi possível identificar o cliente autenticado");
            }

            List<OrderResponseDTO> orders = orderService.getAllOrdersByClientId(clientId);
            return ResponseEntity.ok(orders);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar pedidos: " + e.getMessage());
        }
    }
}
