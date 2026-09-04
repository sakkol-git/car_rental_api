package com.Car_Rental_API.module.sale_order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleOrderVoidRequest {
    @Schema(description = "Optional remark/reason for voiding the order")
    private String remark;
}
