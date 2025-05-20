package br.com.ederoliv.goeat_api.repositories;

import br.com.ederoliv.goeat_api.entities.Support;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupportRepository extends JpaRepository<Support, UUID> {
    Optional<List<Support>> findSupportByPartnerId(UUID partnerId);
    Optional<Support> findByIdAndPartnerId(UUID id, UUID partnerId);
}