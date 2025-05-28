package br.com.ederoliv.goeat_api.dto.analytics;

import java.util.UUID;

public record ProductBestsellerDTO(
        UUID id,
        String name,
        Integer quantity,
        Integer revenue
) {}
