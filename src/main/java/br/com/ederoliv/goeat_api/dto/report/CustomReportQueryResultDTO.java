package br.com.ederoliv.goeat_api.dto.report;

public record CustomReportQueryResultDTO(
        Integer totalSalesFinished,
        Integer totalOrdersFinished,
        Integer totalSalesCanceled,
        Integer totalOrdersCanceled
) {

    public int getAverageTicketCents() {
        if (totalOrdersFinished == null || totalOrdersFinished == 0) {
            return 0;
        }
        return totalSalesFinished / totalOrdersFinished;
    }

    public int getTotalSalesCents() {
        return totalSalesFinished != null ? totalSalesFinished : 0;
    }

    public int getCanceledOrdersCents() {
        return totalSalesCanceled != null ? totalSalesCanceled : 0;
    }

    public int getTotalOrdersFinished() {
        return totalOrdersFinished != null ? totalOrdersFinished : 0;
    }

    public int getTotalOrdersCanceled() {
        return totalOrdersCanceled != null ? totalOrdersCanceled : 0;
    }
}