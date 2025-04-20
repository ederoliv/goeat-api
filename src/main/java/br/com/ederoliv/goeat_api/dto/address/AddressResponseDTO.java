package br.com.ederoliv.goeat_api.dto.address;

import java.util.UUID;

public record AddressResponseDTO(
        UUID id,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        String reference,
        UUID ownerId,
        String ownerType
) {}
