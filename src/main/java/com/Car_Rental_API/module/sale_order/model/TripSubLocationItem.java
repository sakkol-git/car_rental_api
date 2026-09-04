package com.Car_Rental_API.module.sale_order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSubLocationItem {
    private Long id;
    private Long tripId;
    private Long subLocationId;
    private String subLocationName;
    private BigDecimal price;
    private Integer sortOrder;
}

