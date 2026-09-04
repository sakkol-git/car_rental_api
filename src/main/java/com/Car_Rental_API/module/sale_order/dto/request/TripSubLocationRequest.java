package com.Car_Rental_API.module.sale_order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSubLocationRequest {

    @Schema(description = "Sub-location ID", example = "1")
    private Long subLocationId;

    @Schema(description = "Snapshot price for this sub-location", example = "15.00")
    private BigDecimal price;

    @Schema(description = "Sort order index", example = "1")
    private Integer sortOrder;
}
