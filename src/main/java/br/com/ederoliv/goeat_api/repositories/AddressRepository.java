package br.com.ederoliv.goeat_api.repositories;

import br.com.ederoliv.goeat_api.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    Optional<List<Address>> findByClientId(UUID clientId);
    Optional<Address> findByPartnerId(UUID partnerId);
}
