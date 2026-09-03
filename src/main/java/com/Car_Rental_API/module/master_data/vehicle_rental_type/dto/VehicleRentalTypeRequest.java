package com.Car_Rental_API.module.master_data.vehicle_rental_type.dto;

import com.Car_Rental_API.module.master_data.vehicle_rental_type.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.service.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.model.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.*;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleRentalTypeRequest {

    @NotNull(message = "Vehicle Category is required")
    private Long categoryId;

    @NotBlank(message = "Name in Khmer is required")
    private String nameKh;

    @NotBlank(message = "Name in English is required")
    private String nameEn;

    private String nameZh;

    @NotBlank(message = "Location is required")
    private String googleMapUrl;

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
