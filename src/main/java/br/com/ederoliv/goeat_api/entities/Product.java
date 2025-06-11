package br.com.ederoliv.goeat_api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    private int price;

    // Este campo armazena apenas o CID da imagem
    @Column(name = "image_url")
    private String imageUrl;

    //muitos produtos podem pertencer a uma mesma category
    @ManyToOne
    @JoinColumn(name = "category")
    @JsonBackReference // Evita serialização circular
    private Category category;

    //muitos produtos podem pertencer a um mesmo menu
    @ManyToOne
    @JoinColumn(name = "menu_id")
    @JsonBackReference
    private Menu menu;

    // Método auxiliar para obter o ID da categoria
    @JsonProperty("categoryId")
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }

    // Método auxiliar para obter o nome da categoria
    @JsonProperty("categoryName")
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    /**
     * Retorna apenas o CID da imagem armazenado no banco
     * Este método é usado internamente para persistência
     */
    public String getImageCid() {
        return this.imageUrl;
    }

    /**
     * Define o CID da imagem
     * Este método é usado internamente para persistência
     */
    public void setImageCid(String cid) {
        this.imageUrl = cid;
    }

    /**
     * Método auxiliar para verificar se o produto tem uma imagem
     */
    @JsonProperty("hasImage")
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    /**
     * Comentário sobre o campo imageUrl:
     *
     * IMPORTANTE: O campo 'imageUrl' na entidade Product armazena apenas o CID (Content Identifier)
     * da imagem no Pinata IPFS, não a URL completa.
     *
     * Exemplo do que é armazenado:
     * - CID: "bafkreieii6mm36mxhpxt5rvwhq3cdyhhwclzdekh6ppccxegrtu46kxd5a"
     *
     * A URL completa é construída pelo ImageService quando necessário:
     * - URL completa: "https://gateway.pinata.cloud/ipfs/bafkreieii6mm36mxhpxt5rvwhq3cdyhhwclzdekh6ppccxegrtu46kxd5a"
     *
     * Isso permite:
     * 1. Economia de espaço no banco de dados
     * 2. Flexibilidade para mudança de gateway IPFS
     * 3. Reutilização do mesmo CID em diferentes contextos
     *
     * O ProductService é responsável por:
     * - Validar CIDs ao salvar produtos
     * - Construir URLs completas ao retornar produtos para o frontend
     * - Extrair CIDs de URLs completas quando necessário
     */
}