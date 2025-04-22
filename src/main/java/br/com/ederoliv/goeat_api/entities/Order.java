package br.com.ederoliv.goeat_api.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private StatusType orderStatus = StatusType.ESPERANDO;

    @Column(name = "total_price")
    private int totalPrice;


    private String name;
    private String email;
    private String phone;
    private String deliveryAddress;


    @Column(name = "is_authenticated")
    private boolean authenticated = false;

    //relacionamentos
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
}
