package br.com.ederoliv.goeat_api.dto.support;

import br.com.ederoliv.goeat_api.entities.SupportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupportResponseDTO(
        UUID id,
        String title,
        String description,
        SupportStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvedAt,
        UUID partnerId
) {}