package br.com.ederoliv.goeat_api.dto.analytics;

import java.util.List;

public record SalesTimelineResponseDTO(
        List<String> labels,
        List<Integer> revenue,
        List<Integer> orders
) {}