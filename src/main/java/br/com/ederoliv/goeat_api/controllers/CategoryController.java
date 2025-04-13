package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.category.CategoryRequestDTO;
import br.com.ederoliv.goeat_api.services.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/menus/{menuId}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> listAllCategoriesByMenuId(@PathVariable UUID menuId) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.listAllCategoriesByMenuId(menuId));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @PostMapping
    public ResponseEntity<?> register(
            @PathVariable UUID menuId,
            @RequestBody CategoryRequestDTO request) {

        try {
            categoryService.registerCategory(menuId, request);
            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // Ou .body(e.getMessage()) se quiser detalhes
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> delete(
            @PathVariable UUID menuId,
            @PathVariable Long categoryId) {

        try {
            categoryService.deleteCategory(menuId, categoryId);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}