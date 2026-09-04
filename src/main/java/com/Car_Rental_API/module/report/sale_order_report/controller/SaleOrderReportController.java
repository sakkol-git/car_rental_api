package com.Car_Rental_API.module.report.sale_order_report.controller;


import com.Car_Rental_API.module.report.sale_order_report.service.SaleOrderReportService;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.module.report.sale_order_report.dto.request.SaleOrderReportFilterRequest;
import com.Car_Rental_API.module.report.sale_order_report.dto.response.SaleOrderReportRow;
import com.Car_Rental_API.module.report.sale_order_report.dto.response.SaleOrderReportSummary;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

// * Sale Order Report Controller with separated list & summary endpoints
@RestController
@RequestMapping("/reports/sale-orders")
@RequiredArgsConstructor
@Tag(name = "23. Report - Sale Order", description = "Admin: Sale Order Report APIs")
public class SaleOrderReportController extends BaseController {

    private final SaleOrderReportService saleOrderReportService;

    // * 1. Paged list endpoint for sale order report matching filters
    @PostMapping("/list")
    @RequiresPermission(module = "Sale Order Report", action = "View")
    @Operation(summary = "Get sale order report list with pagination", description = "Grouped and ordered by paymentStatus (1 = Booking, 2 = Deposit, 3 = Paid, 4 = Expired / Cancelled), then order ID descending")
    public ResponseEntity<BaseResponse<List<SaleOrderReportRow>>> getReportList(@RequestBody(required = false) SaleOrderReportFilterRequest req) {
        SaleOrderReportFilterRequest filter = req != null ? req : new SaleOrderReportFilterRequest();
        return successPage(saleOrderReportService.getReportList(filter), filter);
    }

    // * 2. Summary metrics endpoint for sale order report matching filters
    @PostMapping("/summary")
    @RequiresPermission(module = "Sale Order Report", action = "View")
    @Operation(summary = "Get sale order report summary metrics", description = "Returns total orders, status counts (toDo, inProgress, complete, void, rejected), total amount, paid, remaining, and discount")
    public ResponseEntity<BaseResponse<SaleOrderReportSummary>> getReportSummary(@RequestBody(required = false) SaleOrderReportFilterRequest req) {
        return success(saleOrderReportService.getReportSummary(req));
    }
}
