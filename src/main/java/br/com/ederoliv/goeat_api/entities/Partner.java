package br.com.ederoliv.goeat_api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "partners")
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //cada Partner terá um e somente um menu
    @OneToOne(mappedBy = "partner", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Menu menu;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String cnpj;

    private String phone;


    @OneToOne(mappedBy = "partner", cascade = CascadeType.ALL)
    @JsonBackReference
    private Address address;

    // Relacionamento com User para autenticação
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<Support> supports;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Order> orders;
}