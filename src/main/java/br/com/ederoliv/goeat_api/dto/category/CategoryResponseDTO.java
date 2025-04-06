package br.com.ederoliv.goeat_api.dto.category;

import java.util.UUID;

public record CategoryResponseDTO(
        Long id,
        String name,
        UUID menuId  // Para confirmar em qual menu foi criada
) {}
