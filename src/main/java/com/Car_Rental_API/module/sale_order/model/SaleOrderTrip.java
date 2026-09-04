package com.Car_Rental_API.module.sale_order.model;

import com.Car_Rental_API.common.base.BaseAuditor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SaleOrderTrip extends BaseAuditor {
    private Long id;
    private Long salesOrderId;

    private Long fromProvinceId;
    private String fromProvinceName;
    private Long toProvinceId;
    private String toProvinceName;

    private BigDecimal price;
    private Integer sortOrder;
    private Integer status; // 1: Complete, 2: In Progress, 3: To Do

    // * Sub-locations selected for this route segment
    private List<TripSubLocationItem> subLocations;
}
