package com.Car_Rental_API.module.sale_order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SaleOrderTripRequest {

    @Schema(description = "Source province ID", example = "1")
    private Long fromProvinceId;

    @Schema(description = "Destination province ID", example = "2")
    private Long toProvinceId;

    @Schema(description = "Selected sub-locations for this route leg")
    private List<TripSubLocationRequest> subLocations;

    @Schema(description = "Leg route price", example = "100.00")
    private BigDecimal price;

    @Schema(description = "Sort order index", example = "1")
    private Integer sortOrder;

    @Schema(description = "Status (1: Complete, 2: In Progress, 3: To Do)", example = "3")
    private Integer status;
}
