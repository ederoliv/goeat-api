package br.com.ederoliv.goeat_api.repositories;

import br.com.ederoliv.goeat_api.entities.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, UUID> {
    Optional<Partner> findByCnpj(String cnpj);
    boolean existsByCnpj(String cnpj);

    // Busca parceiros por ID da categoria
    @Query("SELECT DISTINCT p FROM Partner p JOIN p.restaurantCategories rc WHERE rc.id = :categoryId")
    List<Partner> findPartnersByCategoryId(@Param("categoryId") Long categoryId);

    // Busca parceiros por nome da categoria
    @Query("SELECT DISTINCT p FROM Partner p JOIN p.restaurantCategories rc WHERE LOWER(rc.name) = LOWER(:categoryName)")
    List<Partner> findPartnersByCategoryName(@Param("categoryName") String categoryName);

    // Busca parceiros que possuem pelo menos uma categoria
    @Query("SELECT DISTINCT p FROM Partner p WHERE SIZE(p.restaurantCategories) > 0")
    List<Partner> findPartnersWithCategories();

    // Busca parceiros sem categorias
    @Query("SELECT p FROM Partner p WHERE SIZE(p.restaurantCategories) = 0")
    List<Partner> findPartnersWithoutCategories();
}