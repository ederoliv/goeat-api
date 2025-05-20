package br.com.ederoliv.goeat_api.repositories;

import br.com.ederoliv.goeat_api.entities.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, UUID> {
    List<SupportMessage> findBySupportIdOrderByCreatedAtAsc(UUID supportId);
}