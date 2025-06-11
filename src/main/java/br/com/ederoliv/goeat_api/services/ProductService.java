package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.dto.product.ProductDTO;
import br.com.ederoliv.goeat_api.entities.Category;
import br.com.ederoliv.goeat_api.entities.Menu;
import br.com.ederoliv.goeat_api.entities.Product;
import br.com.ederoliv.goeat_api.repositories.CategoryRepository;
import br.com.ederoliv.goeat_api.repositories.MenuRepository;
import br.com.ederoliv.goeat_api.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final ImageService imageService;

    public String searchProduct(String name){
        Optional<Product> existingProduct = productRepository.findByName(name);

        if (existingProduct.isPresent()){
            return existingProduct.get().getName();
        } else {
            return "produto não existe!";
        }
    }

    public Product registerProduct(Product product){
        return productRepository.save(product);
    }

    public Product updateProduct(UUID id, ProductDTO productDTO) {
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();

            product.setName(productDTO.name());
            product.setDescription(productDTO.description());
            product.setPrice(productDTO.price());

            // Validar e armazenar apenas o CID da imagem
            if (productDTO.imageUrl() != null && !productDTO.imageUrl().isBlank()) {
                // Se for uma URL completa, extrair apenas o CID
                String cid = extractCidFromUrl(productDTO.imageUrl());
                if (imageService.isValidCid(cid)) {
                    product.setImageUrl(cid);
                } else {
                    throw new IllegalArgumentException("CID da imagem inválido");
                }
            } else {
                product.setImageUrl(null);
            }

            if (productDTO.menuId() != null) {
                Optional<Menu> menu = menuRepository.findById(productDTO.menuId());
                menu.ifPresent(product::setMenu);
            }

            if (productDTO.categoryId() != null) {
                Optional<Category> category = categoryRepository.findById(productDTO.categoryId());
                category.ifPresent(product::setCategory);
            } else {
                product.setCategory(null);
            }

            return productRepository.save(product);
        }

        return null;
    }

    public List<Product> listAllProducts(){
        List<Product> products = productRepository.findAll();


        return products.stream()
                .map(this::processProductImages)
                .collect(Collectors.toList());
    }

    public Boolean deleteProduct(UUID id){
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isPresent()) {
            productRepository.delete(optionalProduct.get());
            return true;
        }

        return false;
    }

    public Optional<List<Product>> listAllProductsByMenuId(UUID menuId){
        Optional<List<Product>> productsOpt = productRepository.findProductsByMenuId(menuId);

        if (productsOpt.isPresent()) {
            List<Product> products = productsOpt.get().stream()
                    .map(this::processProductImages)
                    .collect(Collectors.toList());
            return Optional.of(products);
        }

        return Optional.empty();
    }


    private Product processProductImages(Product product) {

        Product processedProduct = new Product();
        processedProduct.setId(product.getId());
        processedProduct.setName(product.getName());
        processedProduct.setDescription(product.getDescription());
        processedProduct.setPrice(product.getPrice());
        processedProduct.setCategory(product.getCategory());
        processedProduct.setMenu(product.getMenu());

        String fullImageUrl = imageService.buildProductImageUrl(product.getImageUrl());
        processedProduct.setImageUrl(fullImageUrl);

        return processedProduct;
    }

    /**
     * Extrai o CID de uma URL completa ou retorna o valor original se já for um CID
     */
    private String extractCidFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }

        String trimmedUrl = imageUrl.trim();


        if (imageService.isValidCid(trimmedUrl)) {
            return trimmedUrl;
        }


        if (trimmedUrl.startsWith("http")) {
            int lastSlashIndex = trimmedUrl.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < trimmedUrl.length() - 1) {
                return trimmedUrl.substring(lastSlashIndex + 1);
            }
        }

        return trimmedUrl;
    }
}