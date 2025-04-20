package br.com.ederoliv.goeat_api.dto.address;

import java.util.UUID;

public record AddressRequestDTO(
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        String reference,
        UUID clientId,
        UUID partnerId
) {}
