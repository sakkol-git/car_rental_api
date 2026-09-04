package com.Car_Rental_API.module.report.dashboard.controller;


import com.Car_Rental_API.module.report.dashboard.service.DashboardService;
import com.Car_Rental_API.common.base.BaseController;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.common.base_dto.request.DueDateFilterRequest;
import com.Car_Rental_API.module.report.dashboard.dto.response.DashboardResponse;
import com.Car_Rental_API.module.report.dashboard.dto.response.DashboardResponse.BookingTrend;
import com.Car_Rental_API.module.report.dashboard.dto.response.DashboardResponse.FleetDistribution;
import com.Car_Rental_API.module.report.dashboard.dto.response.DashboardResponse.RecentOrder;
import com.Car_Rental_API.security.authorization.util.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "20. Dashboard", description = "Dashboard Statistics & Booking Trends APIs")
public class DashboardController extends BaseController {

    private final DashboardService dashboardService;

    @PostMapping
    @RequiresPermission(module = "Dashboard", action = "Summary")
    @Operation(summary = "Get complete dashboard summary (KPIs, booking trend, recent orders, fleet distribution)")
    public ResponseEntity<BaseResponse<DashboardResponse>> getSummary(@RequestBody(required = false) DueDateFilterRequest req) {
        return success(dashboardService.getSummary(req));
    }

    @PostMapping("/booking-trends")
    @RequiresPermission(module = "Dashboard", action = "Booking Trends")
    @Operation(summary = "Get booking trends chart data")
    public ResponseEntity<BaseResponse<List<BookingTrend>>> getBookingTrends(@RequestBody(required = false) DueDateFilterRequest req) {
        return success(dashboardService.getBookingTrends(req));
    }

    @PostMapping("/recent-orders")
    @RequiresPermission(module = "Dashboard", action = "Recent Orders")
    @Operation(summary = "Get recent sale orders list")
    public ResponseEntity<BaseResponse<List<RecentOrder>>> getRecentOrders(@RequestBody(required = false) DueDateFilterRequest req) {
        return success(dashboardService.getRecentOrders(req));
    }

    @PostMapping("/fleet-distribution")
    @RequiresPermission(module = "Dashboard", action = "Fleet Distribution")
    @Operation(summary = "Get fleet distribution by category")
    public ResponseEntity<BaseResponse<List<FleetDistribution>>> getFleetDistribution(@RequestBody(required = false) DueDateFilterRequest req) {
        return success(dashboardService.getFleetDistribution(req));
    }
}
