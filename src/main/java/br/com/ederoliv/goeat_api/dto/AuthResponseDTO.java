package br.com.ederoliv.goeat_api.dto;

import java.util.UUID;

public record AuthResponseDTO(
        String token,
        String name,
        UUID id,
        String role
) {}