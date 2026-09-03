package com.Car_Rental_API.module.master_data.vechicle.dto;

import com.Car_Rental_API.module.master_data.vechicle.repository.*;
import com.Car_Rental_API.module.master_data.vechicle.mapper.*;
import com.Car_Rental_API.module.master_data.vechicle.service.*;
import com.Car_Rental_API.module.master_data.vechicle.model.*;
import com.Car_Rental_API.module.master_data.vechicle.dto.*;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehicleSlideRequest {

    @NotBlank(message = "Slide file name is required")
    private String fileName;

    @NotBlank(message = "Slide image is required")
    private String fileUrl;

    private Integer sortOrder;
}
