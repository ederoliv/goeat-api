package br.com.ederoliv.goeat_api.controllers;

import br.com.ederoliv.goeat_api.dto.support.AddMessageRequestDTO;
import br.com.ederoliv.goeat_api.dto.support.SupportMessageResponseDTO;
import br.com.ederoliv.goeat_api.dto.support.SupportRequestDTO;
import br.com.ederoliv.goeat_api.dto.support.SupportResponseDTO;
import br.com.ederoliv.goeat_api.services.SupportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/supports")
public class SupportController {

    private final SupportService supportService;

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @PostMapping
    public ResponseEntity<?> createSupport(@RequestBody SupportRequestDTO requestDTO, Authentication authentication) {
        try {
            SupportResponseDTO response = supportService.createSupport(requestDTO, authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao criar chamado de suporte", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar chamado: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping
    public ResponseEntity<?> getAllSupports(Authentication authentication) {
        try {
            List<SupportResponseDTO> supports = supportService.getAllSupportsByPartnerId(authentication);
            return ResponseEntity.ok(supports);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao listar chamados de suporte", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar chamados: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @GetMapping("/{supportId}/messages")
    public ResponseEntity<?> getSupportMessages(@PathVariable UUID supportId, Authentication authentication) {
        try {
            List<SupportMessageResponseDTO> messages = supportService.getAllMessagesBySupportId(supportId, authentication);
            return ResponseEntity.ok(messages);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao listar mensagens do chamado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar mensagens: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PARTNER')")
    @PostMapping("/{supportId}/messages")
    public ResponseEntity<?> addMessageToSupport(
            @PathVariable UUID supportId,
            @RequestBody AddMessageRequestDTO requestDTO,
            Authentication authentication) {
        try {
            SupportMessageResponseDTO response = supportService.addMessageToSupport(
                    supportId, requestDTO.content(), authentication);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao adicionar mensagem ao chamado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao adicionar mensagem: " + e.getMessage());
        }
    }
}