package com.Car_Rental_API.module.sale_order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReceiptUpdateRequest {

    @Schema(description = "Payment type: 1 = Bank Paid, 2 = Cash on Hand, 3 = KHQR, 4 = Deposit")
    private Byte paymentType;

    @Schema(description = "Payment status: 1 = Booking, 2 = Deposit, 3 = Paid, 4 = Expired / Cancelled")
    private Byte paymentStatus;

    @Schema(description = "Payment method: 1 = ABA KHQR, 2 = Credit/Debit Card, 3 = ACLEDA, 4 = Wing Bank, 5 = Chip Mong, 6 = Canadia, 7 = Sathapana, 8 = Cash")
    private Integer paymentMethod;

    private BigDecimal paidAmount;
    private BigDecimal depositPrice;

    @Schema(description = "Discount amount applied to the order")
    private BigDecimal discountAmount;

    private String receiptFileName;
    private String receiptFileUrl;

    @Schema(description = "Bank Reference / APV (Approval Code / Transaction Reference number or payment description)")
    private String receiptDescription;
}
