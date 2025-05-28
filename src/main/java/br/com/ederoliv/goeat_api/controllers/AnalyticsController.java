package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.analytics.BestsellersResponseDTO;
import br.com.ederoliv.goeat_api.dto.analytics.DeliveryTypesResponseDTO;
import br.com.ederoliv.goeat_api.dto.analytics.SalesTimelineResponseDTO;
import br.com.ederoliv.goeat_api.services.AnalyticsService;
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
@RequestMapping("api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/sales-timeline")
    public ResponseEntity<?> getSalesTimeline(
            @RequestParam Integer period,
            Authentication authentication) {
        try {
            // Validar parâmetro period
            if (period == null || period < 1 || period > 365) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Parâmetro 'period' deve estar entre 1 e 365 dias");
            }

            log.info("Requisição para sales-timeline com período de {} dias", period);

            SalesTimelineResponseDTO response = analyticsService.getSalesTimeline(period, authentication);
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            log.warn("Acesso não autorizado ao analytics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Parâmetros inválidos para analytics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao gerar dados de analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/products-bestsellers")
    public ResponseEntity<?> getProductsBestsellers(
            @RequestParam String period,
            Authentication authentication) {
        try {
            // Validar parâmetro period
            if (period == null || period.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Parâmetro 'period' é obrigatório");
            }

            Integer periodDays;
            try {
                periodDays = Integer.parseInt(period);
                if (periodDays < 1 || periodDays > 365) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Parâmetro 'period' deve estar entre 1 e 365 dias");
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Parâmetro 'period' deve ser um número válido");
            }

            log.info("Requisição para products-bestsellers com período de {} dias", periodDays);

            BestsellersResponseDTO response = analyticsService.getProductsBestsellers(periodDays, authentication);
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            log.warn("Acesso não autorizado ao products-bestsellers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Parâmetros inválidos para products-bestsellers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao gerar dados de products-bestsellers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/delivery-types")
    public ResponseEntity<?> getDeliveryTypes(
            @RequestParam String period,
            Authentication authentication) {
        try {
            // Validar parâmetro period
            if (period == null || period.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Parâmetro 'period' é obrigatório");
            }

            Integer periodDays;
            try {
                periodDays = Integer.parseInt(period);
                if (periodDays < 1 || periodDays > 365) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Parâmetro 'period' deve estar entre 1 e 365 dias");
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Parâmetro 'period' deve ser um número válido");
            }

            log.info("Requisição para delivery-types com período de {} dias", periodDays);

            DeliveryTypesResponseDTO response = analyticsService.getDeliveryTypes(periodDays, authentication);
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            log.warn("Acesso não autorizado ao delivery-types: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Parâmetros inválidos para delivery-types: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao gerar dados de delivery-types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno do servidor: " + e.getMessage());
        }
    }
}