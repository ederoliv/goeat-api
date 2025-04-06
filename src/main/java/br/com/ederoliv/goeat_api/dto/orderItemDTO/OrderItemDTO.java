package br.com.ederoliv.goeat_api.dto.orderItemDTO;

import java.util.UUID;

public record OrderItemDTO(UUID productId, int quantity) {
}
