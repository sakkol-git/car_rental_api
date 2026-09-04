package com.Car_Rental_API.module.report.sale_order_report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleOrderReportSummary {

    @Schema(description = "Total number of orders matching the filter")
    private long totalOrders;

    // * Order status counts
    @Schema(description = "Count of orders with status 1: To Do")
    private long toDoCount;

    @Schema(description = "Count of orders with status 2: In Progress")
    private long inProgressCount;

    @Schema(description = "Count of orders with status 3: Complete")
    private long completeCount;

    @Schema(description = "Count of orders with status 4: Void")
    private long voidCount;

    @Schema(description = "Count of orders with status 5: Rejected")
    private long rejectedCount;

    // * Payment status counts & grouped deposit / paid metrics
    @Schema(description = "Count of orders with payment status 1: Booking")
    private long bookingCount;

    @Schema(description = "Count of orders with payment status 2: Deposit")
    private long depositCount;

    @Schema(description = "Count of orders with payment status 3: Paid")
    private long paidCount;

    @Schema(description = "Count of orders with payment status 4: Expired / Cancelled")
    private long expiredCount;

    @Schema(description = "Total deposit amount collected from Deposit orders (payment status = 2)")
    private BigDecimal totalDepositAmount;

    @Schema(description = "Total fully paid amount collected from Paid orders (payment status = 3)")
    private BigDecimal totalFullyPaidAmount;

    @Schema(description = "Total unpaid amount for Booking orders (payment status = 1)")
    private BigDecimal totalBookingAmount;

    // * Overall financial aggregates
    @Schema(description = "Sum of total_amount across all matching orders")
    private BigDecimal totalAmount;

    @Schema(description = "Sum of paid_amount across all matching orders")
    private BigDecimal totalPaid;

    @Schema(description = "Sum of remaining_amount across all matching orders")
    private BigDecimal totalRemaining;

    @Schema(description = "Sum of discount_amount across all matching orders")
    private BigDecimal totalDiscount;
}
