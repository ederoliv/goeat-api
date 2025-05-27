package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.report.CustomPeriodFinanceDTO;
import br.com.ederoliv.goeat_api.dto.report.TableReportDTO;
import br.com.ederoliv.goeat_api.services.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/finance")
    public ResponseEntity<?> getTableReport(Authentication authentication) {
        try {
            TableReportDTO tableReport = reportService.getTableReport(authentication);
            return ResponseEntity.ok(tableReport);
        } catch (SecurityException e) {
            log.warn("Acesso não autorizado ao relatório de tabela: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao gerar relatório de tabela", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar relatório de tabela: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/finance/custom")
    public ResponseEntity<?> getCustomPeriodFinance(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        try {
            log.info("Recebendo requisição para relatório customizado: startDate={}, endDate={}", startDate, endDate);
            CustomPeriodFinanceDTO customReport = reportService.getCustomPeriodFinance(startDate, endDate, authentication);
            return ResponseEntity.ok(customReport);
        } catch (SecurityException e) {
            log.warn("Acesso não autorizado ao relatório customizado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Parâmetros inválidos para relatório customizado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao gerar relatório customizado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar relatório customizado: " + e.getMessage());
        }
    }
}