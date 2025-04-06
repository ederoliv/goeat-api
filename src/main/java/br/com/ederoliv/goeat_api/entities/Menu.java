package br.com.ederoliv.goeat_api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "menus")
public class Menu {

    @Id
    @Column(name = "partner_id")
    private UUID id;

    //o menu pertence a um e somente um Partner
    @OneToOne
    @MapsId
    @JoinColumn(name = "partner_id")
    private Partner partner;

    private String description;

    //um unico menu pode conter muitos produtos
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    @JsonIgnore // Evita serialização circular
    private List<Product> products;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    @JsonIgnore // Evita serialização circular
    private List<Category> categories;
}
