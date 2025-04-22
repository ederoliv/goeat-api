package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.entities.*;
import br.com.ederoliv.goeat_api.repositories.*;
import br.com.ederoliv.goeat_api.dto.order.OrderDTO;
import br.com.ederoliv.goeat_api.dto.order.OrderResponseDTO;
import br.com.ederoliv.goeat_api.dto.orderItemDTO.OrderItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final ClientRepository clientRepository;

    private final PartnerRepository partnerRepository;

    private final OrderItemRepository orderItemRepository;


    public List<OrderResponseDTO> getAllOrdersByPartnerId(UUID partnerId) {

        Optional<List<Order>> order = orderRepository.findByPartnerId(partnerId);

        return convertToOrderResponseDTO(order);
    }


    public OrderResponseDTO createOrder(OrderDTO orderDTO, UUID partnerId) {
        // Verifica se o parceiro existe
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        // Criar o pedido
        Order order = new Order();
        order.setPartner(partner);
        order.setOrderStatus(StatusType.ESPERANDO);

        // Dados comuns para todos os pedidos
        order.setName(orderDTO.name());
        order.setEmail(orderDTO.email());
        order.setPhone(orderDTO.phone());
        order.setDeliveryAddress(orderDTO.deliveryAddress());

        // Se clientId não for null, é um pedido autenticado
        if (orderDTO.clientId() != null) {
            Client client = clientRepository.findById(orderDTO.clientId())
                    .orElseThrow(() -> new RuntimeException("Client not found"));
            order.setClient(client);
            order.setAuthenticated(true);
        } else {
            // Caso contrário, é um pedido não autenticado
            order.setClient(null);
            order.setAuthenticated(false);
        }

        // Salvar o pedido inicialmente
        order = orderRepository.save(order);

        // Processar os itens do pedido
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDTO itemDTO : orderDTO.items()) {
            Product product = productRepository.findById(itemDTO.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.quantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setSubtotal(product.getPrice() * itemDTO.quantity());

            orderItems.add(orderItemRepository.save(orderItem));
        }

        // Atualizar o pedido com os itens e o preço total
        order.setItems(orderItems);
        order.setTotalPrice(calculateTotalPrice(orderItems));
        Order savedOrder = orderRepository.save(order);

        // Retornar o DTO de resposta
        return new OrderResponseDTO(
                savedOrder.getId(),
                savedOrder.getOrderStatus(),
                savedOrder.getTotalPrice(),
                savedOrder.getClient() != null ? savedOrder.getClient().getId() : null,
                savedOrder.getPartner().getId(),
                savedOrder.getName(),
                savedOrder.getDeliveryAddress()
        );
    }


    public OrderResponseDTO updateOrderStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setOrderStatus(getOrderStatusType(status));

        return toOrderResponseDTO(orderRepository.save(order));

    }


    private int calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .mapToInt(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }


    public List<OrderResponseDTO> convertToOrderResponseDTO(Optional<List<Order>> optionalOrders) {
        return optionalOrders
                .map(orders -> orders.stream()
                        .map(this::toOrderResponseDTO)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }


    private OrderResponseDTO toOrderResponseDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getOrderStatus(),
                order.getTotalPrice(),
                order.getClient() != null ? order.getClient().getId() : null,
                order.getPartner().getId(),
                order.getName(),
                order.getDeliveryAddress()
        );
    }

    public StatusType getOrderStatusType(String status) {

        StatusType newStatus = StatusType.ESPERANDO;

        newStatus = switch (status) {
            case "ESPERANDO" -> StatusType.ESPERANDO;
            case "PREPARANDO" -> StatusType.PREPARANDO;
            case "ENCAMINHADOS" -> StatusType.ENCAMINHADOS;
            case "FINALIZADOS" -> StatusType.FINALIZADOS;
            default -> newStatus;
        };

        return newStatus;
    }
}

