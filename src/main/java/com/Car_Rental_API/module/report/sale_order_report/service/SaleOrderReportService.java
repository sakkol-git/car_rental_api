package com.Car_Rental_API.module.report.sale_order_report.service;


import com.Car_Rental_API.module.report.sale_order_report.repository.SaleOrderReportRepository;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.Car_Rental_API.common.base_dto.response.PageResult;
import com.Car_Rental_API.module.report.sale_order_report.dto.request.SaleOrderReportFilterRequest;
import com.Car_Rental_API.module.report.sale_order_report.dto.response.SaleOrderReportRow;
import com.Car_Rental_API.module.report.sale_order_report.dto.response.SaleOrderReportSummary;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleOrderReportService {

    private final SaleOrderReportRepository reportRepository;

    // * Get paged sale order report list matching filter
    @Cacheable(value = "saleOrderReportList", key = "#req != null ? #req.toString() : 'default'")
    public PageResult<SaleOrderReportRow> getReportList(SaleOrderReportFilterRequest req) {
        SaleOrderReportFilterRequest filter = req != null ? req : new SaleOrderReportFilterRequest();
        long total = reportRepository.countAll(filter);
        List<SaleOrderReportRow> list = reportRepository.findAll(filter);
        return new PageResult<>(list, total);
    }

    // * Get report summary metrics matching filter (total orders, status totals, amount, paid, remaining, discount)
    @Cacheable(value = "saleOrderReportSummary", key = "#req != null ? #req.toString() : 'default'")
    public SaleOrderReportSummary getReportSummary(SaleOrderReportFilterRequest req) {
        SaleOrderReportFilterRequest filter = req != null ? req : new SaleOrderReportFilterRequest();
        return reportRepository.getSummary(filter);
    }
}
