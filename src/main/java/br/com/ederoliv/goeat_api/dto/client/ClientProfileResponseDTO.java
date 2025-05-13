package br.com.ederoliv.goeat_api.dto.client;

import java.time.LocalDate;
import java.util.UUID;

public record ClientProfileResponseDTO(
        UUID id,
        String name,
        String email,
        String cpf,
        String phone,
        LocalDate birthDate
) {}
