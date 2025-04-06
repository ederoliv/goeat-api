package br.com.ederoliv.goeat_api.services;


import br.com.ederoliv.goeat_api.dto.category.CategoryRequestDTO;
import br.com.ederoliv.goeat_api.entities.Category;
import br.com.ederoliv.goeat_api.entities.Menu;
import br.com.ederoliv.goeat_api.repositories.CategoryRepository;
import br.com.ederoliv.goeat_api.repositories.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;

    public Category findById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public List<Category> findAllByMenuId(UUID menuId) {
        Optional<List<Category>> categories = categoryRepository.findAllByMenuId(menuId);
        return categories.orElse(List.of());
    }

    public List<Category> listAllCategoriesByMenuId(UUID menuId) {
        Optional<List<Category>> categories = categoryRepository.findAllCategoriesByMenuId(menuId);
        return categories.orElse(List.of());
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category registerCategory(UUID menuId, CategoryRequestDTO categoryRequestDTO) {
        Optional<Menu> menuOpt = menuRepository.findById(menuId);

        if (menuOpt.isPresent()) {
            Menu menu = menuOpt.get();

            Category category = new Category();
            category.setName(categoryRequestDTO.name());
            category.setMenu(menu);

            return categoryRepository.save(category);
        }

        return null;
    }

    public Category updateCategory(Long id, Category updatedCategory) {
        Optional<Category> existingCategory = categoryRepository.findById(id);

        if (existingCategory.isPresent()) {
            Category category = existingCategory.get();
            category.setName(updatedCategory.getName());
            return categoryRepository.save(category);
        }

        return null;
    }

    public boolean deleteCategory(Long id) {
        Optional<Category> category = categoryRepository.findById(id);

        if (category.isPresent()) {
            categoryRepository.delete(category.get());
            return true;
        }

        return false;
    }

    public boolean deleteCategory(UUID menuId, Long categoryId) {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);

        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();

            // Verificar se a categoria pertence ao menu especificado
            if (category.getMenu() != null && category.getMenu().getId().equals(menuId)) {
                categoryRepository.delete(category);
                return true;
            }
        }

        return false;
    }
}
