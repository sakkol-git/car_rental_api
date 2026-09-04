package com.Car_Rental_API.module.report.customer_review.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerReview {
    private Long id;
    private Long customerId;
    private Long salesOrderId;
    private Long vehicleId;
    private BigDecimal ratingStars;
    private String comment;
    private Byte isDisabled;
    private Byte isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modified;
    private Long modifiedBy;

    // * Display fields
    private String customerName;
    private String customerPhone;
    private String orderNo;
    private String vehicleName;
}
