package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.report.ReportValueDTO;
import br.com.ederoliv.goeat_api.services.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/daily")
    public ResponseEntity<?> getDailyReport(Authentication authentication) {
        try {
            int dailyTotal = reportService.getDailyReport(authentication);
            return ResponseEntity.ok(new ReportValueDTO(dailyTotal));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao gerar relatório diário", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar relatório diário: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyReport(Authentication authentication) {
        try {
            int weeklyTotal = reportService.getWeeklyReport(authentication);
            return ResponseEntity.ok(new ReportValueDTO(weeklyTotal));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao gerar relatório semanal", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar relatório semanal: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReport(Authentication authentication) {
        try {
            int monthlyTotal = reportService.getMonthlyReport(authentication);
            return ResponseEntity.ok(new ReportValueDTO(monthlyTotal));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao gerar relatório mensal", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar relatório mensal: " + e.getMessage());
        }
    }
}
