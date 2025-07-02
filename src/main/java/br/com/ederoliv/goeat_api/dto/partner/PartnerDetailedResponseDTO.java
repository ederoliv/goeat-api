package br.com.ederoliv.goeat_api.dto.partner;

import br.com.ederoliv.goeat_api.dto.operatingHours.OperatingHoursResponseDTO;

import java.util.List;
import java.util.UUID;

public record PartnerDetailedResponseDTO(
        UUID id,
        String name,
        boolean isOpen,
        String fullAddress,
        List<OperatingHoursResponseDTO> operatingHours
) {}