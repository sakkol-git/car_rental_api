package com.Car_Rental_API.module.master_data.vechicle.dto;

import com.Car_Rental_API.module.master_data.vechicle.repository.*;
import com.Car_Rental_API.module.master_data.vechicle.mapper.*;
import com.Car_Rental_API.module.master_data.vechicle.service.*;
import com.Car_Rental_API.module.master_data.vechicle.model.*;
import com.Car_Rental_API.module.master_data.vechicle.dto.*;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleFacilityRequest {

    @NotNull(message = "Facility is required")
    private Long facilityId;

    @NotNull(message = "Facility quantity is required")
    @Min(value = 1, message = "Facility quantity must be >= 1")
    private Integer qty;
}
