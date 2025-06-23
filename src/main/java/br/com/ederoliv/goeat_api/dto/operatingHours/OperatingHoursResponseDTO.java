package br.com.ederoliv.goeat_api.dto.operatingHours;

import java.time.DayOfWeek;

public record OperatingHoursResponseDTO(
        Long id,
        DayOfWeek dayOfWeek,
        boolean isOpen,
        String openingTime,
        String closingTime
) {}
