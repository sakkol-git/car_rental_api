package com.Car_Rental_API.module.report.sale_order_report.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleOrderReportResponse {
    private List<SaleOrderReportRow> list;
    private long total;
    private SaleOrderReportSummary summary;
}
