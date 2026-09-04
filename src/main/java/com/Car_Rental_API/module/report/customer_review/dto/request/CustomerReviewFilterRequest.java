package com.Car_Rental_API.module.report.customer_review.dto.request;

import com.Car_Rental_API.common.base_dto.request.DueDateFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CustomerReviewFilterRequest extends DueDateFilterRequest {
    private Long customerId;
    private Long vehicleId;
    private Long salesOrderId;
}
