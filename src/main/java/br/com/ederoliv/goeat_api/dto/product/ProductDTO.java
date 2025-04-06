package br.com.ederoliv.goeat_api.dto.product;

import java.util.UUID;

public record ProductDTO(
        String name,
        String description,
        int price,
        String imageUrl,
        UUID menuId,
        Long categoryId
) {}
