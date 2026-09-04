package com.Car_Rental_API.module.report.sale_order_report.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class SaleOrderReportRow {
    private Long id;
    private String orderNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;

    private Byte orderType;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long vehicleId;
    private String vehicleName;

    // * Rental Details
    private Long vehicleCategoryId;
    private String vehicleCategoryNameKh;
    private String vehicleCategoryNameEn;
    private String vehicleCategoryNameZh;
    private String vehicleRentalTypeName;
    private Byte journeyType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String pickupLocationName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pickupTime;

    private String dropoffLocationName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dropoffTime;

    private Integer amountOfPeople;
    private Integer amountOfVehicles;
    private String remark;
    private String voidRemark;

    // * Payment Breakdown & Status
    private BigDecimal subtotalAmount;
    private BigDecimal serviceFee;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal depositPrice;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;

    @Schema(description = "Payment type: 1 = Bank Paid, 2 = Cash on Hand, 3 = KHQR, 4 = Deposit")
    private Byte paymentType;

    @Schema(description = "Payment status: 1 = Booking, 2 = Deposit, 3 = Paid, 4 = Expired / Cancelled")
    private Byte paymentStatus;

    @Schema(description = "Bank Reference / APV (Approval Code / Transaction Reference)")
    private String receiptDescription;

    private String currency;
    private Byte orderStatus;
}
