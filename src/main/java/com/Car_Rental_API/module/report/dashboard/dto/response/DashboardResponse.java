package com.Car_Rental_API.module.report.dashboard.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DashboardResponse {
    // * Summary KPIs
    private long totalOrders;
    private long totalToDo;
    private long totalInProgress;
    private long totalComplete;
    private BigDecimal totalRevenue;

    // * Booking trend by month (12 months)
    private List<BookingTrend> bookingTrends;

    // * Recent orders preview
    private List<RecentOrder> recentOrders;

    // * Fleet distribution by vehicle category
    private List<FleetDistribution> fleetDistributions;

    @Getter
    @Setter
    public static class BookingTrend {
        private String month;   // e.g. "Jan", "Feb"
        private int year;
        private long count;
    }

    @Getter
    @Setter
    public static class RecentOrder {
        private String orderNo;
        private String customerName;
        private String customerPhone;
        private String vehicleName;
        private Integer amountOfVehicles;
        private Byte orderStatus;
    }

    @Getter
    @Setter
    public static class FleetDistribution {
        private Long vehicleCategoryId;
        private String vehicleCategoryName;
        private long vehicleCount;
        private double percentage;
    }
}
