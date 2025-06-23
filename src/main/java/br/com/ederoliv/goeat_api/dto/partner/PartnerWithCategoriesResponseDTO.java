package br.com.ederoliv.goeat_api.dto.partner;

import br.com.ederoliv.goeat_api.dto.restaurantCategory.RestaurantCategoryResponseDTO;

import java.util.List;
import java.util.UUID;

public record PartnerWithCategoriesResponseDTO(
        UUID id,
        String name,
        String phone,
        boolean isOpen,
        List<RestaurantCategoryResponseDTO> categories
) {}