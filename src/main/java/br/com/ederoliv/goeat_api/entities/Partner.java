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

    // Alterado de boolean (primitivo) para Boolean (objeto)
    // Isso permite valores nulos, e definimos um valor padrão no getter
    @Column(name = "is_open")
    private Boolean isOpen;

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

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OperatingHours> operatingHours;

    // Relacionamento many-to-many com RestaurantCategory
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "partner_restaurant_category",
            joinColumns = @JoinColumn(name = "partner_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_category_id")
    )
    @JsonManagedReference
    private List<RestaurantCategory> restaurantCategories;

    // Getter com valor padrão para evitar NPE
    public boolean isOpen() {
        return isOpen != null ? isOpen : true; // Default para true se for null
    }

    // Setter modificado para garantir que nunca será null
    public void setOpen(Boolean open) {
        this.isOpen = open != null ? open : true;
    }
}