package br.com.ederoliv.goeat_api.dto.restaurantCategory;

import java.util.List;

public record PartnerCategoryUpdateDTO(
        List<Long> categoryIds
) {}