package br.com.ederoliv.goeat_api.services;


import br.com.ederoliv.goeat_api.dto.report.*;
import br.com.ederoliv.goeat_api.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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

    public CustomPeriodFinanceDTO getCustomPeriodFinance(String startDate, String endDate, Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        log.info("Gerando relatório customizado para parceiro: {} - Período: {} a {}", partnerId, startDate, endDate);

        try {
            // Parse das datas
            LocalDate startLocalDate = LocalDate.parse(startDate);
            LocalDate endLocalDate = LocalDate.parse(endDate);

            // Validação das datas
            if (startLocalDate.isAfter(endLocalDate)) {
                throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
            }

            LocalDateTime startDateTime = startLocalDate.atStartOfDay();
            LocalDateTime endDateTime = endLocalDate.atTime(LocalTime.MAX);

            log.info("Datas convertidas - Início: {}, Fim: {}", startDateTime, endDateTime);

            // Buscar dados do período customizado
            CustomReportQueryResultDTO customData = orderRepository.findCustomReportData(
                    partnerId, startDateTime, endDateTime);

            log.info("Dados retornados: vendas={}, pedidos={}, cancelamentos={}, pedidos cancelados={}",
                    customData.getTotalSalesCents(),
                    customData.getTotalOrdersFinished(),
                    customData.getCanceledOrdersCents(),
                    customData.getTotalOrdersCanceled());

            CustomPeriodFinanceDTO result = new CustomPeriodFinanceDTO(
                    customData.getTotalSalesCents(),
                    customData.getAverageTicketCents(),
                    customData.getCanceledOrdersCents(),
                    customData.getTotalOrdersFinished(),
                    customData.getTotalOrdersCanceled()
            );

            log.info("Relatório customizado gerado com sucesso para parceiro: {}", partnerId);
            return result;

        } catch (DateTimeParseException e) {
            log.error("Erro ao fazer parse das datas: startDate={}, endDate={}", startDate, endDate, e);
            throw new IllegalArgumentException("Formato de data inválido. Use o formato: YYYY-MM-DD");
        }
    }
}
