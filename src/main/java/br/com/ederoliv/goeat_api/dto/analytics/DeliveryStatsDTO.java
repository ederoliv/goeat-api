package br.com.ederoliv.goeat_api.dto.analytics;

public record DeliveryStatsDTO(
        Integer deliveryOrders,
        Integer deliveryRevenue,
        Integer pickupOrders,
        Integer pickupRevenue
) {}