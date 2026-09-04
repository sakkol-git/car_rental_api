package com.Car_Rental_API.module.sale_order.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SaleOrderResponse {
    private Long id;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long vehicleId;
    private String vehicleName;
    private String vehicleFileName;
    private String vehicleFileUrl;
    private Long vehicleModelId;
    private String vehicleModelName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    private String passengerName;
    private String passengerPhone;
    private Long passengerNationalityId;
    private String nationalityName;
    private Integer amountOfPeople;
    private Integer amountOfVehicles;
    private String remark;
    private String voidRemark;
    private Long vehicleCategoryId;
    private String vehicleCategoryNameKh;
    private String vehicleCategoryNameEn;
    private String vehicleCategoryNameZh;
    private Long vehicleRentalTypeId;
    private String vehicleRentalTypeName;
    private Byte journeyType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String pickupAddress;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pickupTime;

    private String dropoffAddress;
    private BigDecimal dropoffLatitude;
    private BigDecimal dropoffLongitude;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dropoffTime;

    private BigDecimal subtotalAmount;
    private BigDecimal serviceFee;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal depositPrice;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;

    @Schema(description = "Order type: 1 = APP, 2 = SYSTEM")
    private Byte orderType;

    @Schema(description = "Payment type: 1 = Bank Paid, 2 = Cash on Hand, 3 = KHQR, 4 = Deposit")
    private Byte paymentType;

    @Schema(description = "Payment status: 1 = Booking, 2 = Deposit, 3 = Paid, 4 = Expired / Cancelled")
    private Byte paymentStatus;

    @Schema(description = "Payment method: 1 = ABA KHQR, 2 = Credit/Debit Card, 3 = ACLEDA, 4 = Wing Bank, 5 = Chip Mong, 6 = Canadia, 7 = Sathapana, 8 = Cash")
    private Integer paymentMethod;
    private Long transactionId;
    private String currency;
    private String receiptFileName;
    private String receiptFileUrl;

    @Schema(description = "Bank Reference / APV (Approval Code / Transaction Reference)")
    private String receiptDescription;

    @Schema(description = "Order status: 1 = To Do, 2 = In Progress, 3 = Complete, 4 = Void")
    private Byte orderStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modified;
    private String modifiedBy;

    private List<SaleOrderTripResponse> trips = new ArrayList<>();
    private List<SaleOrderPaymentHistoryResponse> paymentHistories = new ArrayList<>();
}
