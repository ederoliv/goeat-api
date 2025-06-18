package br.com.ederoliv.goeat_api.dto.partner;

import br.com.ederoliv.goeat_api.dto.restaurantCategory.RestaurantCategoryResponseDTO;

import java.util.List;
import java.util.UUID;

public record PartnerDetailsResponseDTO(
        UUID id,
        String name,
        String cnpj,
        String phone,
        String address,
        List<RestaurantCategoryResponseDTO> categories
) {}
