package br.com.ederoliv.goeat_api.dto.operatingHours;

import java.time.DayOfWeek;
import java.util.List;


public record OperatingHoursUpdateDTO(
        List<OperatingHoursDayDTO> schedules
) {

    public record OperatingHoursDayDTO(
            DayOfWeek dayOfWeek,
            boolean isOpen,
            String openingTime,
            String closingTime
    ) {}
}
