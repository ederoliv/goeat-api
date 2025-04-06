package br.com.ederoliv.goeat_api.dto.order;

import br.com.ederoliv.goeat_api.entities.StatusType;

import java.util.UUID;

public record OrderResponseDTO(Long id, StatusType orderStatus, int totalPrice, UUID clientId, UUID partnerId) {
}
