package br.com.ederoliv.goeat_api.services;

import br.com.ederoliv.goeat_api.entities.StatusType;
import br.com.ederoliv.goeat_api.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final AuthenticationUtilsService authenticationUtilsService;

    /**
     * Calcula o total de vendas do dia atual para o parceiro autenticado
     */
    public int getDailyReport(Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return orderRepository.findTotalFinishedOrdersByPartnerAndDateRange(
                partnerId, StatusType.FINALIZADOS, startOfDay, endOfDay
        ).orElse(0);
    }

    /**
     * Calcula o total de vendas da semana atual para o parceiro autenticado
     */
    public int getWeeklyReport(Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        // Calcula o início e fim da semana atual
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        LocalDate startOfWeek = today.with(weekFields.dayOfWeek(), 1);
        LocalDate endOfWeek = today.with(weekFields.dayOfWeek(), 7);

        LocalDateTime startOfWeekDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endOfWeekDateTime = endOfWeek.atTime(LocalTime.MAX);

        return orderRepository.findTotalFinishedOrdersByPartnerAndDateRange(
                partnerId, StatusType.FINALIZADOS, startOfWeekDateTime, endOfWeekDateTime
        ).orElse(0);
    }

    /**
     * Calcula o total de vendas do mês atual para o parceiro autenticado
     */
    public int getMonthlyReport(Authentication authentication) {
        UUID partnerId = authenticationUtilsService.getPartnerIdFromAuthentication(authentication);
        if (partnerId == null) {
            throw new SecurityException("Não foi possível identificar o parceiro autenticado");
        }

        // Calcula o início e fim do mês atual
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        LocalDateTime startOfMonthDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endOfMonthDateTime = endOfMonth.atTime(LocalTime.MAX);

        return orderRepository.findTotalFinishedOrdersByPartnerAndDateRange(
                partnerId, StatusType.FINALIZADOS, startOfMonthDateTime, endOfMonthDateTime
        ).orElse(0);
    }
}
