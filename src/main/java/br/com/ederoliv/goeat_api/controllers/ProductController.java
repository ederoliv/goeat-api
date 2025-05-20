package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.product.ProductDTO;
import br.com.ederoliv.goeat_api.entities.Category;
import br.com.ederoliv.goeat_api.entities.Menu;
import br.com.ederoliv.goeat_api.entities.Product;
import br.com.ederoliv.goeat_api.services.CategoryService;
import br.com.ederoliv.goeat_api.services.MenuService;
import br.com.ederoliv.goeat_api.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/partners/{partnerId}/products")
public class ProductController {

    private final MenuService menuService;
    private final ProductService productService;
    private final CategoryService categoryService;

    /*
    @GetMapping
    public ResponseEntity<List<Product>> listAllProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.listAllProducts());
    }*/

    @GetMapping()
    public ResponseEntity<?> getAllProductsByPartnerId(@PathVariable UUID partnerId) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.listAllProductsByMenuId(partnerId));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @PostMapping
    public ResponseEntity<?> register(@RequestBody ProductDTO productDTO) {
        try {
            Menu menu = menuService.findById(productDTO.menuId());

            if (menu != null) {
                // Criar um novo objeto Product
                Product product = new Product();

                product.setName(productDTO.name());
                product.setDescription(productDTO.description());
                product.setPrice(productDTO.price());
                product.setImageUrl(productDTO.imageUrl());
                product.setMenu(menu);

                // Associar a categoria, se fornecida
                if (productDTO.categoryId() != null) {
                    Category category = categoryService.findById(productDTO.categoryId());
                    if (category != null) {
                        product.setCategory(category);
                    }
                }

                // Salvar o produto usando o service
                productService.registerProduct(product);

                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_PARTNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        if(productService.deleteProduct(id)){
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody ProductDTO productDTO) {
        try {
            Product updatedProduct = productService.updateProduct(id, productDTO);

            if (updatedProduct != null) {
                return ResponseEntity.ok(updatedProduct);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}