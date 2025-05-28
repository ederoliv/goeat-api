package br.com.ederoliv.goeat_api.repositories;


import br.com.ederoliv.goeat_api.dto.analytics.DailySalesDTO;
import br.com.ederoliv.goeat_api.dto.analytics.DeliveryStatsDTO;
import br.com.ederoliv.goeat_api.dto.analytics.ProductBestsellerDTO;
import br.com.ederoliv.goeat_api.dto.report.CustomReportQueryResultDTO;
import br.com.ederoliv.goeat_api.dto.report.ReportQueryResultDTO;
import br.com.ederoliv.goeat_api.entities.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    @Query("SELECT new br.com.ederoliv.goeat_api.dto.report.CustomReportQueryResultDTO(" +
            "CAST(COALESCE(SUM(CASE WHEN o.orderStatus = 'FINALIZADOS' AND o.finishedAt >= :startDate AND o.finishedAt <= :endDate THEN o.totalPrice ELSE 0 END), 0) AS integer), " +
            "CAST(COALESCE(COUNT(CASE WHEN o.orderStatus = 'FINALIZADOS' AND o.finishedAt >= :startDate AND o.finishedAt <= :endDate THEN 1 END), 0) AS integer), " +
            "CAST(COALESCE(SUM(CASE WHEN o.orderStatus = 'CANCELADOS' AND o.canceledAt >= :startDate AND o.canceledAt <= :endDate THEN o.totalPrice ELSE 0 END), 0) AS integer), " +
            "CAST(COALESCE(COUNT(CASE WHEN o.orderStatus = 'CANCELADOS' AND o.canceledAt >= :startDate AND o.canceledAt <= :endDate THEN 1 END), 0) AS integer)" +
            ") FROM Order o " +
            "WHERE o.partner.id = :partnerId")
    CustomReportQueryResultDTO findCustomReportData(
            @Param("partnerId") UUID partnerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT new br.com.ederoliv.goeat_api.dto.analytics.DailySalesDTO(" +
            "CAST(DATE(o.finishedAt) AS java.time.LocalDate), " +
            "CAST(COALESCE(SUM(o.totalPrice), 0) AS integer), " +
            "CAST(COALESCE(COUNT(o.id), 0) AS integer)" +
            ") FROM Order o " +
            "WHERE o.partner.id = :partnerId " +
            "AND DATE(o.finishedAt) BETWEEN :startDate AND :endDate " +
            "AND o.orderStatus = 'FINALIZADOS' " +
            "GROUP BY DATE(o.finishedAt) " +
            "ORDER BY DATE(o.finishedAt) ASC")
    List<DailySalesDTO> findDailySalesForPeriod(
            @Param("partnerId") UUID partnerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT new br.com.ederoliv.goeat_api.dto.analytics.ProductBestsellerDTO(" +
            "p.id, " +
            "p.name, " +
            "CAST(COALESCE(SUM(oi.quantity), 0) AS integer), " +
            "CAST(COALESCE(SUM(oi.quantity * oi.unitPrice), 0) AS integer)" +
            ") FROM Order o " +
            "INNER JOIN o.items oi " +
            "INNER JOIN oi.product p " +
            "WHERE o.partner.id = :partnerId " +
            "AND o.finishedAt >= :startDate " +
            "AND o.finishedAt <= :endDate " +
            "AND o.orderStatus = 'FINALIZADOS' " +
            "GROUP BY p.id, p.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<ProductBestsellerDTO> findProductsBestsellersByPeriod(
            @Param("partnerId") UUID partnerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT new br.com.ederoliv.goeat_api.dto.analytics.DeliveryStatsDTO(" +
            "CAST(COALESCE(COUNT(CASE WHEN o.deliveryAddress != 'RETIRADA NO LOCAL' THEN 1 END), 0) AS integer), " +
            "CAST(COALESCE(SUM(CASE WHEN o.deliveryAddress != 'RETIRADA NO LOCAL' THEN o.totalPrice ELSE 0 END), 0) AS integer), " +
            "CAST(COALESCE(COUNT(CASE WHEN o.deliveryAddress = 'RETIRADA NO LOCAL' THEN 1 END), 0) AS integer), " +
            "CAST(COALESCE(SUM(CASE WHEN o.deliveryAddress = 'RETIRADA NO LOCAL' THEN o.totalPrice ELSE 0 END), 0) AS integer)" +
            ") FROM Order o " +
            "WHERE o.partner.id = :partnerId " +
            "AND o.finishedAt >= :startDate " +
            "AND o.finishedAt <= :endDate " +
            "AND o.orderStatus = 'FINALIZADOS'")
    DeliveryStatsDTO findDeliveryTypesByPeriod(
            @Param("partnerId") UUID partnerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

