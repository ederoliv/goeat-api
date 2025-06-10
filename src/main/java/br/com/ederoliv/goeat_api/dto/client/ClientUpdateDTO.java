package br.com.ederoliv.goeat_api.dto.client;

import java.time.LocalDate;

public record ClientUpdateDTO(
        String name,
        String phone,
        LocalDate birthDate,
        String profileImage
) {}