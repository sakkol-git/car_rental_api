package com.Car_Rental_API.module.master_data.journey_price.dto;

import com.Car_Rental_API.module.master_data.journey_price.repository.*;
import com.Car_Rental_API.module.master_data.journey_price.mapper.*;
import com.Car_Rental_API.module.master_data.journey_price.service.*;
import com.Car_Rental_API.module.master_data.journey_price.model.*;
import com.Car_Rental_API.module.master_data.journey_price.dto.*;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class JourneyPriceRequest {

    @NotNull(message = "Province From is required")
    private Long fromProvinceId;

    @NotNull(message = "Province To is required")
    private Long toProvinceId;

    private String description;

    @Valid
    @NotEmpty(message = "Vehicle price list is required")
    private List<JourneyVehiclePriceRequest> prices;
}
