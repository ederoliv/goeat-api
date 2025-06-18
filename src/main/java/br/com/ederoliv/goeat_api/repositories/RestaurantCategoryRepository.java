package br.com.ederoliv.goeat_api.repositories;

import br.com.ederoliv.goeat_api.entities.RestaurantCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantCategoryRepository extends JpaRepository<RestaurantCategory, Long> {

    Optional<RestaurantCategory> findByName(String name);

    // Busca todas as categorias que um partner possui
    @Query("SELECT rc FROM RestaurantCategory rc JOIN rc.partners p WHERE p.id = :partnerId")
    List<RestaurantCategory> findByPartnerId(@Param("partnerId") UUID partnerId);

    // Busca todas as categorias que ainda não foram associadas a um partner
    @Query("SELECT rc FROM RestaurantCategory rc WHERE rc.id NOT IN " +
            "(SELECT rc2.id FROM RestaurantCategory rc2 JOIN rc2.partners p WHERE p.id = :partnerId)")
    List<RestaurantCategory> findAvailableForPartner(@Param("partnerId") UUID partnerId);
}