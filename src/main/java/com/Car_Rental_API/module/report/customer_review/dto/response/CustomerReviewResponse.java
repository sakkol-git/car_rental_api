package com.Car_Rental_API.module.report.customer_review.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerReviewResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long salesOrderId;
    private String orderNo;
    private Long vehicleId;
    private String vehicleName;
    private BigDecimal ratingStars;
    private String comment;
    private Byte isDisabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
}
