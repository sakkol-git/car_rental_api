package com.Car_Rental_API.module.master_data.vehicle_brand.dto;

import com.Car_Rental_API.module.master_data.vehicle_brand.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.service.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.model.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.dto.*;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehicleBrandRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private String fileName;

    @NotBlank(message = "Icon is required")
    private String fileUrl;

    private Integer sortOrder;
}
