package br.com.ederoliv.goeat_api.dto.client;

import java.time.LocalDate;

public record ClientRegisterDTO(
        String name,
        String email,
        String password,
        String cpf,
        String phone,
        LocalDate birthDate
) {}