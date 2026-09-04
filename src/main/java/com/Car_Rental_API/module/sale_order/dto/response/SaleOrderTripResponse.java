package com.Car_Rental_API.module.sale_order.dto.response;

import com.Car_Rental_API.module.sale_order.model.TripSubLocationItem;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SaleOrderTripResponse {
    private Long id;
    private Long salesOrderId;

    private Long fromProvinceId;
    private String fromProvinceName;
    private Long toProvinceId;
    private String toProvinceName;

    private Integer status;
    private BigDecimal price;
    private Integer sortOrder;

    private List<TripSubLocationItem> subLocations;
}
