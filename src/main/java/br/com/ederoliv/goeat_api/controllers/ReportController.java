package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.report.ReportValueDTO;
import br.com.ederoliv.goeat_api.dto.report.TableReportDTO;
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


}
