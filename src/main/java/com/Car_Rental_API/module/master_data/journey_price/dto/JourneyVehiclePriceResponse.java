package com.Car_Rental_API.module.master_data.journey_price.dto;

import com.Car_Rental_API.module.master_data.journey_price.repository.*;
import com.Car_Rental_API.module.master_data.journey_price.mapper.*;
import com.Car_Rental_API.module.master_data.journey_price.service.*;
import com.Car_Rental_API.module.master_data.journey_price.model.*;
import com.Car_Rental_API.module.master_data.journey_price.dto.*;


import com.fasterxml.jackson.annotation.JsonProperty;
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
public class JourneyVehiclePriceResponse {
    private Long id;
    private Long vehicleModelId;
    private String vehicleModelName;
    private BigDecimal oneWayPrice;
    private BigDecimal roundTripPrice;
    private BigDecimal oneDayTourPrice;
    private BigDecimal multiCityPrice;

    @Schema(description = "City Tour Price (journeyType=5)")
    private BigDecimal cityTourPrice;

    @JsonProperty("pricePerDay")
    public BigDecimal getPricePerDay() {
        return this.cityTourPrice;
    }

    @JsonProperty("pricePerDay")
    public void setPricePerDay(BigDecimal pricePerDay) {
        this.cityTourPrice = pricePerDay;
    }
}
