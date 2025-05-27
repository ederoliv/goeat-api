package br.com.ederoliv.goeat_api.repositories;


import br.com.ederoliv.goeat_api.dto.report.ReportQueryResultDTO;
import br.com.ederoliv.goeat_api.entities.Order;

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


    @Query("SELECT new br.com.ederoliv.goeat_api.dto.report.ReportQueryResultDTO(" +
            "CAST(COALESCE(SUM(CASE WHEN o.orderStatus = 'FINALIZADOS' AND o.finishedAt >= :startDate AND o.finishedAt <= :endDate THEN o.totalPrice ELSE 0 END), 0) AS integer), " +
            "CAST(COALESCE(COUNT(CASE WHEN o.orderStatus = 'FINALIZADOS' AND o.finishedAt >= :startDate AND o.finishedAt <= :endDate THEN 1 END), 0) AS integer), " +
            "CAST(COALESCE(SUM(CASE WHEN o.orderStatus = 'CANCELADOS' AND o.canceledAt >= :startDate AND o.canceledAt <= :endDate THEN o.totalPrice ELSE 0 END), 0) AS integer)" +
            ") FROM Order o " +
            "WHERE o.partner.id = :partnerId")
    ReportQueryResultDTO findTableReportData(
            @Param("partnerId") UUID partnerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


}

