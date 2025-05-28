package br.com.ederoliv.goeat_api.dto.analytics;

public record DeliveryTypesResponseDTO(
        DeliveryTypeDTO delivery,
        DeliveryTypeDTO pickup
) {}