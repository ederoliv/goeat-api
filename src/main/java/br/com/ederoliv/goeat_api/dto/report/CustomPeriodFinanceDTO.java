package br.com.ederoliv.goeat_api.dto.report;

public record CustomPeriodFinanceDTO(
        int totalSales,
        int averageTicket,
        int canceledOrdersValue,
        int totalOrders,
        int canceledOrders
) {}
