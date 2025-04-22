package br.com.ederoliv.goeat_api.dto.order;


import br.com.ederoliv.goeat_api.dto.orderItemDTO.OrderItemDTO;

import java.util.List;
import java.util.UUID;

public record OrderDTO(
        UUID clientId,

        List<OrderItemDTO> items,

        String name,
        String email,
        String phone,
        String deliveryAddress


) {}
