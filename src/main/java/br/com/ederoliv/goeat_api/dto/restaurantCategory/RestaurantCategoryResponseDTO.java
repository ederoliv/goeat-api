package br.com.ederoliv.goeat_api.dto.restaurantCategory;

public record RestaurantCategoryResponseDTO(
        Long id,
        String name,
        String description
) {}