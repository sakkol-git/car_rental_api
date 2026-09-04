package com.Car_Rental_API.module.report.sale_order_report.dto.request;

import com.Car_Rental_API.common.base_dto.request.DueDateFilterRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SaleOrderReportFilterRequest extends DueDateFilterRequest {

    @Schema(description = "Order type: 1 = APP, 2 = SYSTEM")
    private Byte orderType;

    @Schema(description = "Order status: 1 = To Do, 2 = In Progress, 3 = Complete, 4 = Void, 5 = Rejected")
    private Byte orderStatus;

    @Schema(description = "Payment status: 1 = Booking, 2 = Deposit, 3 = Paid, 4 = Expired / Cancelled")
    private Byte paymentStatus;

    @Schema(description = "Vehicle category ID (References vehicle_categories.id)")
    private Long vehicleCategoryId;

    @Schema(description = "Vehicle rental type ID (References vehicle_rental_types.id)")
    private Long vehicleRentalTypeId;

    @Schema(description = "Journey trip type: 1 = One-Way, 2 = One-Day-Tour, 3 = Round-Trip, 4 = Multi-City, 5 = City Tour")
    private Byte journeyType;

    @Schema(description = "Payment type: 1 = Bank Paid, 2 = Cash on Hand, 3 = KHQR, 4 = Deposit")
    private Byte paymentType;

    @Schema(description = "Payment method: 1 = ABA KHQR, 2 = Credit/Debit Card, 3 = ACLEDA, 4 = Wing Bank, 5 = Chip Mong, 6 = Canadia, 7 = Sathapana, 8 = Cash")
    private Integer paymentMethod;

    private Long pickupLocationId;
    private Long dropoffLocationId;

    private Long customerId;
    private Long createdBy;
    private Long vehicleId;

    private String createdFrom;
    private String createdTo;
}
