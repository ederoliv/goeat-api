package br.com.ederoliv.goeat_api.repositories;

import br.com.ederoliv.goeat_api.entities.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, UUID> {
    Optional<Partner> findByCnpj(String cnpj);
    boolean existsByCnpj(String cnpj);
}