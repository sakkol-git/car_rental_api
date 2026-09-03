package com.Car_Rental_API.module.master_data.vehicle_model.dto;

import com.Car_Rental_API.module.master_data.vehicle_model.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_model.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_model.service.*;
import com.Car_Rental_API.module.master_data.vehicle_model.model.*;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.*;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleModelRequest {

    @NotNull(message = "Brand is required")
    private Long brandId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
