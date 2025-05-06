package br.com.ederoliv.goeat_api.services;


import br.com.ederoliv.goeat_api.dto.orderItemDTO.OrderItemResponseDTO;
import br.com.ederoliv.goeat_api.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderRepository orderRepository;

    public List<OrderItemResponseDTO> getOrderItems(Long orderId) {

        return orderRepository.findById(orderId)
                .map(order -> order.getItems().stream()
                        .map(item -> new OrderItemResponseDTO(
                                item.getProduct().getName(),
                                item.getQuantity()
                        ))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
