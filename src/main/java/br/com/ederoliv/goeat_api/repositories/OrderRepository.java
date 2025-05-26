package br.com.ederoliv.goeat_api.repositories;


import br.com.ederoliv.goeat_api.entities.Order;
import br.com.ederoliv.goeat_api.entities.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<List<Order>> findByPartnerId(UUID id);

    /**
     * Calcula o total de vendas (soma dos totalPrice) dos pedidos finalizados
     * de um parceiro específico em um período de tempo
     */
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o " +
            "WHERE o.partner.id = :partnerId " +
            "AND o.orderStatus = :status " +
            "AND o.createdAt >= :startDate " +
            "AND o.createdAt <= :endDate")
    Optional<Integer> findTotalFinishedOrdersByPartnerAndDateRange(
            @Param("partnerId") UUID partnerId,
            @Param("status") StatusType status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

