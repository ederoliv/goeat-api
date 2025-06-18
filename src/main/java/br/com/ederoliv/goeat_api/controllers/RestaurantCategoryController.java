package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.restaurantCategory.PartnerCategoryUpdateDTO;
import br.com.ederoliv.goeat_api.dto.restaurantCategory.RestaurantCategoryResponseDTO;
import br.com.ederoliv.goeat_api.services.RestaurantCategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/restaurant-categories")
public class RestaurantCategoryController {

    private final RestaurantCategoryService restaurantCategoryService;

    /**
     * Lista todas as categorias de restaurante disponíveis (endpoint público)
     */
    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            List<RestaurantCategoryResponseDTO> categories = restaurantCategoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Erro ao listar categorias de restaurante", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar categorias: " + e.getMessage());
        }
    }

    /**
     * Lista as categorias do parceiro autenticado
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/my-categories")
    public ResponseEntity<?> getMyCategories(Authentication authentication) {
        try {
            List<RestaurantCategoryResponseDTO> categories =
                    restaurantCategoryService.getPartnerCategories(authentication);
            return ResponseEntity.ok(categories);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao listar categorias do parceiro", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar suas categorias: " + e.getMessage());
        }
    }

    /**
     * Lista as categorias disponíveis para o parceiro (que ele ainda não possui)
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableCategories(Authentication authentication) {
        try {
            List<RestaurantCategoryResponseDTO> categories =
                    restaurantCategoryService.getAvailableCategoriesForPartner(authentication);
            return ResponseEntity.ok(categories);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao listar categorias disponíveis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar categorias disponíveis: " + e.getMessage());
        }
    }

    /**
     * Atualiza as categorias do parceiro (substitui todas)
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @PutMapping("/my-categories")
    public ResponseEntity<?> updateMyCategories(
            @RequestBody PartnerCategoryUpdateDTO updateDTO,
            Authentication authentication) {
        try {
            List<RestaurantCategoryResponseDTO> updatedCategories =
                    restaurantCategoryService.updatePartnerCategories(updateDTO, authentication);
            return ResponseEntity.ok(updatedCategories);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao atualizar categorias do parceiro", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar categorias: " + e.getMessage());
        }
    }

    /**
     * Adiciona uma categoria ao parceiro
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @PostMapping("/{categoryId}/add")
    public ResponseEntity<?> addCategoryToMyRestaurant(
            @PathVariable Long categoryId,
            Authentication authentication) {
        try {
            List<RestaurantCategoryResponseDTO> updatedCategories =
                    restaurantCategoryService.addCategoryToPartner(categoryId, authentication);
            return ResponseEntity.ok(updatedCategories);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao adicionar categoria ao parceiro", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao adicionar categoria: " + e.getMessage());
        }
    }

    /**
     * Remove uma categoria do parceiro
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @DeleteMapping("/{categoryId}/remove")
    public ResponseEntity<?> removeCategoryFromMyRestaurant(
            @PathVariable Long categoryId,
            Authentication authentication) {
        try {
            List<RestaurantCategoryResponseDTO> updatedCategories =
                    restaurantCategoryService.removeCategoryFromPartner(categoryId, authentication);
            return ResponseEntity.ok(updatedCategories);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao remover categoria do parceiro", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao remover categoria: " + e.getMessage());
        }
    }
}