package br.com.ederoliv.goeat_api.dto.report;

public record TableReportDTO(
        PeriodMetricsDTO daily,
        PeriodMetricsDTO weekly,
        PeriodMetricsDTO monthly
) {}
