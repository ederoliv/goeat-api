package br.com.ederoliv.goeat_api.dto.support;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupportMessageResponseDTO(
        UUID id,
        String content,
        boolean fromSupport,
        LocalDateTime createdAt
) {}