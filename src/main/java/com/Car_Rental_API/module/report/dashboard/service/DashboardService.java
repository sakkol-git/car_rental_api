package com.Car_Rental_API.module.report.dashboard.service;


import com.Car_Rental_API.module.report.dashboard.repository.DashboardRepository;
import com.Car_Rental_API.common.base_dto.request.DueDateFilterRequest;
import com.Car_Rental_API.module.report.dashboard.dto.response.DashboardResponse;
import com.Car_Rental_API.module.report.dashboard.dto.response.DashboardResponse.BookingTrend;
import com.Car_Rental_API.module.report.dashboard.dto.response.DashboardResponse.FleetDistribution;
import com.Car_Rental_API.module.report.dashboard.dto.response.DashboardResponse.RecentOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    // * Full summary (KPIs + Booking Trends + Recent Orders + Fleet Distribution)
    public DashboardResponse getSummary(DueDateFilterRequest req) {
        return dashboardRepository.buildSummary(req);
    }

    // * Booking Trends chart
    public List<BookingTrend> getBookingTrends(DueDateFilterRequest req) {
        return dashboardRepository.getBookingTrends(req);
    }

    // * Recent Sale Orders preview
    public List<RecentOrder> getRecentOrders(DueDateFilterRequest req) {
        return dashboardRepository.getRecentOrders(req);
    }

    // * Fleet Distribution donut chart
    public List<FleetDistribution> getFleetDistribution(DueDateFilterRequest req) {
        return dashboardRepository.getFleetDistribution(req);
    }
}
