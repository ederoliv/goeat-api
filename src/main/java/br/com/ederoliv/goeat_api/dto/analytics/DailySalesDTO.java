package br.com.ederoliv.goeat_api.dto.analytics;

import java.time.LocalDate;

public record DailySalesDTO(
        LocalDate orderDate,
        Integer dailyRevenue,
        Integer dailyOrders
) {}
