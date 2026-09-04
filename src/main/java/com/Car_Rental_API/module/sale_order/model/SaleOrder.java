package com.Car_Rental_API.module.sale_order.model;


import com.Car_Rental_API.common.base.BaseAuditor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SaleOrder extends BaseAuditor {
    private Long id;
    private String orderNo;
    private Long customerId;
    private Long vehicleId;
    private Byte orderType; // 1: APP, 2: SYSTEM
    private LocalDateTime orderDate;
    private String passengerName;
    private String passengerPhone;
    private Long passengerNationalityId;
    private Integer amountOfPeople;
    private Integer amountOfVehicles;
    private String remark;
    private String voidRemark;
    private Long vehicleCategoryId;
    private Long vehicleRentalTypeId;
    private Byte journeyType; // 1: One-Way, 2: One-Day-Tour, 3: Round-Trip, 4: Multi-City, 5: City Tour
    private LocalDate startDate;
    private LocalDate endDate;
    private String pickupAddress;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;
    private LocalDateTime pickupTime;
    private String dropoffAddress;
    private BigDecimal dropoffLatitude;
    private BigDecimal dropoffLongitude;
    private LocalDateTime dropoffTime;
    private BigDecimal subtotalAmount;
    private BigDecimal serviceFee;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal depositPrice;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private Byte paymentType; // 1: Bank Paid, 2: Cash on Hand, 3: KHQR, 4: Deposit
    private Byte paymentStatus; // 1: Pending, 2: Partial / Deposit Paid, 3: Paid, 4: Expired / Cancelled
    private Integer paymentMethod; // 1: ABA KHQR, 2: Credit/Debit Card, 3: ACLEDA, 4: Wing Bank, 5: Chip Mong, 6: Canadia, 7: Sathapana, 8: Cash
    private Long transactionId;
    private String currency;
    private String receiptFileName;
    private String receiptFileUrl;
    private String receiptDescription;
    private Byte orderStatus; // 1: To Do, 2: In Progress, 3: Complete, 4: Void

    // Display fields for UI
    private String customerName;
    private String customerPhone;
    private String vehicleName;
    private String vehicleFileName;
    private String vehicleFileUrl;
    private Long vehicleModelId;
    private String vehicleModelName;
    private String vehicleCategoryNameKh;
    private String vehicleCategoryNameEn;
    private String vehicleCategoryNameZh;
    private String vehicleRentalTypeName;
    private String nationalityName;

    private List<SaleOrderTrip> trips = new ArrayList<>();
    private List<SaleOrderPaymentHistory> paymentHistories = new ArrayList<>();
}

