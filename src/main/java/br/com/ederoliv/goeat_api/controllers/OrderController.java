package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.orderItemDTO.OrderItemResponseDTO;
import br.com.ederoliv.goeat_api.services.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/orders")
public class OrderController {

    private final OrderItemService orderItemService;

    @PreAuthorize( "hasAnyAuthority('SCOPE_ROLE_CLIENT', 'SCOPE_ROLE_PARTNER')")
    @GetMapping("/{orderId}/items")
    public ResponseEntity<?> getOrderItems(@PathVariable Long orderId) {
        List<OrderItemResponseDTO> items = orderItemService.getOrderItems(orderId);
        return ResponseEntity.ok(items);
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_CLIENT')")
    @GetMapping("/clients/{clientId}")
    public ResponseEntity<?> getOrdersByClientId() {
        return ResponseEntity.ok("Ok");
    }
}
