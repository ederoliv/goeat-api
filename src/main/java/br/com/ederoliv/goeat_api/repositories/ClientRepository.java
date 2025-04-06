package br.com.ederoliv.goeat_api.repositories;

import br.com.ederoliv.goeat_api.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
}