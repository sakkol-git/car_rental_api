package com.Car_Rental_API.module.master_data.journey_price.dto;

import com.Car_Rental_API.module.master_data.journey_price.repository.*;
import com.Car_Rental_API.module.master_data.journey_price.mapper.*;
import com.Car_Rental_API.module.master_data.journey_price.service.*;
import com.Car_Rental_API.module.master_data.journey_price.model.*;
import com.Car_Rental_API.module.master_data.journey_price.dto.*;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class JourneyVehiclePriceRequest {

    @NotNull(message = "Vehicle is required")
    private Long vehicleModelId;

    @NotNull(message = "One Way Price is required")
    @DecimalMin(value = "0.00", message = "One Way Price must be >= 0")
    private BigDecimal oneWayPrice;

    @NotNull(message = "Round Trip Price is required")
    @DecimalMin(value = "0.00", message = "Round Trip Price must be >= 0")
    private BigDecimal roundTripPrice;

    @NotNull(message = "One Day Tour Price is required")
    @DecimalMin(value = "0.00", message = "One Day Tour Price must be >= 0")
    private BigDecimal oneDayTourPrice;

    @NotNull(message = "Multi City Price is required")
    @DecimalMin(value = "0.00", message = "Multi City Price must be >= 0")
    private BigDecimal multiCityPrice;

    @NotNull(message = "City Tour Price is required")
    @DecimalMin(value = "0.00", message = "City Tour Price must be >= 0")
    @Schema(description = "City Tour Price (formerly Price/Day, journeyType=5)")
    private BigDecimal cityTourPrice;

    // * Alias getter/setter for backwards compatibility with pricePerDay
    @JsonProperty("pricePerDay")
    public BigDecimal getPricePerDay() {
        return this.cityTourPrice;
    }

    @JsonProperty("pricePerDay")
    public void setPricePerDay(BigDecimal pricePerDay) {
        if (pricePerDay != null) {
            this.cityTourPrice = pricePerDay;
        }
    }
}
