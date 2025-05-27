package br.com.ederoliv.goeat_api.dto.report;

public record PeriodMetricsDTO(
        int averageTicket,
        int totalSales,
        int canceledOrdersValue
) {}
