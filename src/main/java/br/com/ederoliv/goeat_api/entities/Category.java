package br.com.ederoliv.goeat_api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "menuId")
    @JsonBackReference
    private Menu menu;

    //uma categoria pode conter muitos produtos
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonIgnore // Evita serialização circular
    private List<Product> products;


    public Category(String name, Menu menu) {
        this.name = name;
        this.menu = menu;
    }

}
