package com.Car_Rental_API.module.master_data.vechicle.dto;

import com.Car_Rental_API.module.master_data.vechicle.repository.*;
import com.Car_Rental_API.module.master_data.vechicle.mapper.*;
import com.Car_Rental_API.module.master_data.vechicle.service.*;
import com.Car_Rental_API.module.master_data.vechicle.model.*;
import com.Car_Rental_API.module.master_data.vechicle.dto.*;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehicleItemRequest {

    @NotBlank(message = "Vehicle item code is required")
    private String code;

    @NotBlank(message = "Vehicle item plate number is required")
    private String plateNumber;

    private Integer status;
}
