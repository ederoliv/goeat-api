package br.com.ederoliv.goeat_api.repositories;

import br.com.ederoliv.goeat_api.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
    Optional<List<Category>> findAllByMenuId(UUID menuId);
    Optional<List<Category>> findAllCategoriesByMenuId(UUID menuId);

}
