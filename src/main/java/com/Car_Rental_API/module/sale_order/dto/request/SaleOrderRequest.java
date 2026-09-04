package com.Car_Rental_API.module.sale_order.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SaleOrderRequest {

    private Long customerId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    private String passengerName;
    private String passengerPhone;
    private Long passengerNationalityId;
    private Integer amountOfPeople;
    private Integer amountOfVehicles;
    private String remark;

    @Schema(description = "Vehicle category ID (References vehicle_categories.id)")
    private Long vehicleCategoryId;
    private Long vehicleRentalTypeId;

    @NotNull(message = "Journey type is required")
    private Byte journeyType;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String pickupAddress;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;

    @NotNull(message = "Pickup time is required")
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

    @Schema(description = "Payment type: 1 = Bank Paid, 2 = Cash on Hand, 3 = KHQR, 4 = Deposit")
    private Byte paymentType;

    @Schema(description = "Payment status: 1 = Booking, 2 = Deposit, 3 = Paid, 4 = Expired / Cancelled")
    private Byte paymentStatus;

    @Schema(description = "Payment method: 1 = ABA KHQR, 2 = Credit/Debit Card, 3 = ACLEDA, 4 = Wing Bank, 5 = Chip Mong, 6 = Canadia, 7 = Sathapana, 8 = Cash")
    private Integer paymentMethod;
    private String currency;
    private String receiptFileName;
    private String receiptFileUrl;

    @Schema(description = "Bank Reference / APV (Approval Code / Transaction Reference)")
    private String receiptDescription;

    private List<SaleOrderTripRequest> trips = new ArrayList<>();
}
