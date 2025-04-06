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
        // Verifica se o cliente existe
        Client client = clientRepository.findById(orderDTO.clientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        // Verifica se o parceiro existe
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        // 3. Criar o pedido
        Order order = new Order();
        order.setClient(client);
        order.setPartner(partner);
        order.setOrderStatus(StatusType.ESPERANDO);  // Status inicial

        // 4. Salvar o pedido no banco (sem itens por enquanto)
        order = orderRepository.save(order);

        // 5. Adicionar os itens ao pedido
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDTO itemDTO : orderDTO.items()) {
            Product product = productRepository.findById(itemDTO.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.quantity());

            // Adicione o item ao banco
            orderItems.add(orderItemRepository.save(orderItem));
        }

        // 6. Associar os itens ao pedido e atualizar o pedido
        order.setItems(orderItems);
        order.setTotalPrice(calculateTotalPrice(orderItems));  // Calcular o preço total

        Order orderSaved = orderRepository.save(order);

        // 7. Monta o DTO de resposta

        return new OrderResponseDTO(
                orderSaved.getId(),
                orderSaved.getOrderStatus(),
                orderSaved.getTotalPrice(),
                orderSaved.getClient().getId(),
                orderSaved.getPartner().getId());
    }

    public OrderResponseDTO updateOrderStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setOrderStatus(getOrderStatusType(status));

        return toOrderResponseDTO(orderRepository.save(order));

    }

    // outros métodos....

    // Método para calcular o preço total
    private int calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .mapToInt(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    public List<OrderResponseDTO> convertToOrderResponseDTO(Optional<List<Order>> optionalOrders) {
        return optionalOrders
                .map(orders -> orders.stream()  // Se a lista de pedidos estiver presente, realiza o stream
                        .map(this::toOrderResponseDTO)  // Converte cada Order em OrderResponseDTO
                        .collect(Collectors.toList()))  // Coleta em uma lista de OrderResponseDTO
                .orElse(Collections.emptyList());  // Caso o Optional esteja vazio, retorna uma lista vazia
    }

    private OrderResponseDTO toOrderResponseDTO(Order order) {

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderStatus(),
                order.getTotalPrice(),
                order.getClient().getId(),
                order.getPartner().getId());
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

