package br.com.ederoliv.goeat_api.dto.analytics;

import java.util.List;

public record BestsellersResponseDTO(
        List<ProductBestsellerDTO> products
) {}
