package br.com.ederoliv.goeat_api.dto.operatingHours;

import java.util.List;


public record OperatingHoursFullResponseDTO(
        List<OperatingHoursResponseDTO> schedules,
        boolean isOpenNow,
        boolean manuallyOpen
) {}
