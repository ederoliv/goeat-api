package br.com.ederoliv.goeat_api.services;


import br.com.ederoliv.goeat_api.dto.report.PeriodMetricsDTO;
import br.com.ederoliv.goeat_api.dto.report.ReportQueryResultDTO;
import br.com.ederoliv.goeat_api.dto.report.TableReportDTO;
import br.com.ederoliv.goeat_api.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final AuthenticationUtilsService authenticationUtilsService;

    public TableReportDTO getTableReport(Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        log.info("Gerando relatório de tabela para parceiro: {}", partnerId);


        LocalDate today = LocalDate.now();


        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);


        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate startOfWeek = today.with(weekFields.dayOfWeek(), 1);
        LocalDate endOfWeek = today.with(weekFields.dayOfWeek(), 7);
        LocalDateTime startOfWeekDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endOfWeekDateTime = endOfWeek.atTime(LocalTime.MAX);


        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        LocalDateTime startOfMonthDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endOfMonthDateTime = endOfMonth.atTime(LocalTime.MAX);


        ReportQueryResultDTO dailyData = orderRepository.findTableReportData(
                partnerId, startOfDay, endOfDay);

        ReportQueryResultDTO weeklyData = orderRepository.findTableReportData(
                partnerId, startOfWeekDateTime, endOfWeekDateTime);

        ReportQueryResultDTO monthlyData = orderRepository.findTableReportData(
                partnerId, startOfMonthDateTime, endOfMonthDateTime);


        PeriodMetricsDTO dailyMetrics = new PeriodMetricsDTO(
                dailyData.getAverageTicketCents(),
                dailyData.getTotalSalesCents(),
                dailyData.getCanceledOrdersCents()
        );

        PeriodMetricsDTO weeklyMetrics = new PeriodMetricsDTO(
                weeklyData.getAverageTicketCents(),
                weeklyData.getTotalSalesCents(),
                weeklyData.getCanceledOrdersCents()
        );

        PeriodMetricsDTO monthlyMetrics = new PeriodMetricsDTO(
                monthlyData.getAverageTicketCents(),
                monthlyData.getTotalSalesCents(),
                monthlyData.getCanceledOrdersCents()
        );

        log.info("Relatório de tabela gerado com sucesso para parceiro: {}", partnerId);

        return new TableReportDTO(dailyMetrics, weeklyMetrics, monthlyMetrics);
    }
}
