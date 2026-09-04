package com.Car_Rental_API.module.report.customer_review.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CustomerReviewRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    private Long salesOrderId;

    @NotNull(message = "Rating is required")
    private BigDecimal ratingStars; // 1.0 – 5.0

    private String comment;
}
