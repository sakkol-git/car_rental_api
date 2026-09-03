package com.Car_Rental_API.module.master_data.sub_location.dto;

import com.Car_Rental_API.module.master_data.sub_location.repository.*;
import com.Car_Rental_API.module.master_data.sub_location.mapper.*;
import com.Car_Rental_API.module.master_data.sub_location.service.*;
import com.Car_Rental_API.module.master_data.sub_location.model.*;
import com.Car_Rental_API.module.master_data.sub_location.dto.*;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubLocationResponse {
    private Long id;
    private Long provinceId;
    private String provinceName;
    private String name;

    @Schema(description = "Sub-location default price (used when no journey route price found)")
    private BigDecimal defaultPrice;

    @Schema(description = "Journey route price for ONE_WAY (journeyType=1)")
    private BigDecimal oneWayPrice;

    @Schema(description = "Journey route price for ONE_DAY_TOUR (journeyType=2)")
    private BigDecimal oneDayTourPrice;

    @Schema(description = "Journey route price for ROUND_TRIP (journeyType=3)")
    private BigDecimal roundTripPrice;

    @Schema(description = "Journey route price for MULTI_CITY (journeyType=4)")
    private BigDecimal multiCityPrice;

    @Schema(description = "Journey route price for CITY_TOUR (journeyType=5)")
    private BigDecimal cityTourPrice;

    @Schema(description = "Journey route unit price per day (journeyType=5)")
    private BigDecimal pricePerDay;

    @Schema(description = "Highlighted price for selected journeyType (or defaultPrice if no route found)")
    private BigDecimal price;

    private String googleMapUrl;
    private String description;
    private Byte isPublic;

    private LocalDateTime created;
    private String createdBy;
    private LocalDateTime modified;
    private String modifiedBy;
}
