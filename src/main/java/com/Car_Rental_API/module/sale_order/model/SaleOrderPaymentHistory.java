package com.Car_Rental_API.module.sale_order.model;



import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SaleOrderPaymentHistory {
    private Long id;
    private Long salesOrderId;
    private Byte paymentType; // 1: Bank Paid, 2: Cash on Hand, 3: KHQR, 4: Deposit
    private Integer paymentMethod; // 1: ABA KHQR, 2: Credit Card, 3: ACLEDA, 4: Wing, 5: Chip Mong, 6: Canadia, 7: Sathapana, 8: Cash
    private Byte paymentStage; // 1: Initial Deposit, 2: Balance Payment, 3: Full Payment, 4: Price Revision (trip update — no money exchanged)
    private BigDecimal amount;
    private String receiptFileName;
    private String receiptFileUrl;
    private String receiptDescription;
    private LocalDateTime created;
    private Long createdBy;
    private Integer status; // 1: Pending, 2: Paid, 3: Cancelled / Expired
    private Integer isActive = 1;
}

