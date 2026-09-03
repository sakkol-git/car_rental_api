package com.Car_Rental_API.module.master_data.vehicle_category.dto;

import com.Car_Rental_API.module.master_data.vehicle_category.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_category.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_category.service.*;
import com.Car_Rental_API.module.master_data.vehicle_category.model.*;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.*;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehicleCategoryRequest {

    @NotBlank(message = "Name in Khmer is required")
    private String nameKh;

    @NotBlank(message = "Name in English is required")
    private String nameEn;

    private String nameZh;

    @NotBlank(message = "Description in Khmer is required")
    private String descriptionKh;

    @NotBlank(message = "Description in English is required")
    private String descriptionEn;

    private String descriptionZh;

    private String fileName;
    
    @NotBlank(message = "Image is required")
    private String fileUrl;

    private Integer sortOrder;
}
