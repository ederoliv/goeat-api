package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.operatingHours.OperatingHoursFullResponseDTO;
import br.com.ederoliv.goeat_api.dto.operatingHours.OperatingHoursRequestDTO;
import br.com.ederoliv.goeat_api.dto.operatingHours.OperatingHoursResponseDTO;
import br.com.ederoliv.goeat_api.dto.operatingHours.OperatingHoursUpdateDTO;
import br.com.ederoliv.goeat_api.services.OperatingHoursService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/operating-hours")
public class OperatingHoursController {

    private final OperatingHoursService operatingHoursService;

    /**
     * Pega os horários de funcionamento do parceiro autenticado
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping
    public ResponseEntity<?> getOperatingHours(Authentication authentication) {
        try {
            UUID partnerId = getPartnerIdFromAuthentication(authentication);
            OperatingHoursFullResponseDTO response = operatingHoursService.getOperatingHours(partnerId);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao buscar horários de funcionamento", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar horários: " + e.getMessage());
        }
    }

    /**
     * Endpoint público para verificar os horários de funcionamento de um parceiro
     */
    @GetMapping("/partners/{partnerId}")
    public ResponseEntity<?> getPartnerOperatingHours(@PathVariable UUID partnerId) {
        try {
            OperatingHoursFullResponseDTO response = operatingHoursService.getOperatingHours(partnerId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao buscar horários de funcionamento do parceiro {}", partnerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar horários: " + e.getMessage());
        }
    }

    /**
     * Atualiza os horários de funcionamento do parceiro autenticado
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @PutMapping
    public ResponseEntity<?> updateOperatingHours(
            @RequestBody OperatingHoursUpdateDTO updateDTO,
            Authentication authentication) {
        try {
            OperatingHoursFullResponseDTO response = operatingHoursService.updateOperatingHours(
                    updateDTO, authentication);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao atualizar horários de funcionamento", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar horários: " + e.getMessage());
        }
    }

    /**
     * Atualiza o status aberto/fechado do parceiro
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @PutMapping("/status")
    public ResponseEntity<?> updatePartnerOpenStatus(
            @RequestBody Map<String, Boolean> requestBody,
            Authentication authentication) {
        try {
            Boolean isOpen = requestBody.get("isOpen");
            if (isOpen == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("O campo 'isOpen' é obrigatório");
            }

            operatingHoursService.updatePartnerOpenStatus(isOpen, authentication);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao atualizar status do parceiro", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar status: " + e.getMessage());
        }
    }

    /**
     * Atualiza ou adiciona o horário de funcionamento para um dia específico
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @PutMapping("/{dayOfWeek}")
    public ResponseEntity<?> updateDayOperatingHours(
            @PathVariable DayOfWeek dayOfWeek,
            @RequestBody OperatingHoursRequestDTO requestDTO,
            Authentication authentication) {
        try {
            UUID partnerId = getPartnerIdFromAuthentication(authentication);
            OperatingHoursResponseDTO response = operatingHoursService
                    .addOrUpdateOperatingHours(partnerId, dayOfWeek, requestDTO);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao atualizar horário do dia {}", dayOfWeek, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar horário: " + e.getMessage());
        }
    }

    /**
     * Utilitário para extrair o ID do parceiro da autenticação
     */
    private UUID getPartnerIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof
                org.springframework.security.oauth2.jwt.Jwt) {
            org.springframework.security.oauth2.jwt.Jwt jwt =
                    (org.springframework.security.oauth2.jwt.Jwt) authentication.getPrincipal();
            String partnerIdStr = jwt.getClaim("partnerId");
            if (partnerIdStr != null && !partnerIdStr.isEmpty()) {
                return UUID.fromString(partnerIdStr);
            }
        }
        throw new SecurityException("Não foi possível identificar o parceiro autenticado");
    }
}