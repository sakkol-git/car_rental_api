package com.Car_Rental_API.module.sale_order.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SaleOrderPaymentHistoryResponse {
    private Long id;
    private Long salesOrderId;

    @Schema(description = "Payment type: 1 = Bank Paid, 2 = Cash on Hand, 3 = KHQR, 4 = Deposit")
    private Byte paymentType;

    @Schema(description = "Payment method: 1 = ABA KHQR, 2 = Credit/Debit Card, 3 = ACLEDA, 4 = Wing Bank, 5 = Chip Mong, 6 = Canadia, 7 = Sathapana, 8 = Cash")
    private Integer paymentMethod;

    @Schema(description = "Payment stage: 1 = Initial Deposit, 2 = Balance Payment, 3 = Full Payment")
    private Byte paymentStage;

    private BigDecimal amount;
    private String receiptFileName;
    private String receiptFileUrl;

    @Schema(description = "Bank Reference / APV (Approval Code / Transaction Reference)")
    private String receiptDescription;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    private Integer status;
    private Integer isActive;
}
