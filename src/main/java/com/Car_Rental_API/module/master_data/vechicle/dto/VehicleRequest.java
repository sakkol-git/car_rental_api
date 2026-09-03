package com.Car_Rental_API.module.master_data.vechicle.dto;

import com.Car_Rental_API.module.master_data.vechicle.repository.*;
import com.Car_Rental_API.module.master_data.vechicle.mapper.*;
import com.Car_Rental_API.module.master_data.vechicle.service.*;
import com.Car_Rental_API.module.master_data.vechicle.model.*;
import com.Car_Rental_API.module.master_data.vechicle.dto.*;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class VehicleRequest {

    @NotNull(message = "Brand is required")
    private Long brandId;

    @NotNull(message = "Model is required")
    private Long modelId;

    @NotBlank(message = "Name in Khmer is required")
    private String nameKh;

    @NotBlank(message = "Name in English is required")
    private String nameEn;

    private String nameZh;
    private String vehicleCode;
    private String plateNumber;
    private String fileName;

    @NotBlank(message = "Image is required")
    private String fileUrl;

    @NotNull(message = "Vehicle quantity is required")
    @Min(value = 1, message = "Vehicle quantity must be >= 1")
    private Integer quantity;

    @Min(value = 1, message = "Passenger capacity must be >= 1")
    private Integer passengers;

    private Long isPublic;

    @NotEmpty(message = "Vehicle Category is required")
    private List<Long> categoryIds;

    @NotEmpty(message = "Vehicle Rental Type is required")
    private List<Long> rentalTypeIds;

    @Valid
    @NotEmpty(message = "Slide Show is required")
    private List<VehicleSlideRequest> slides;

    @Valid
    private List<VehicleFacilityRequest> facilities;

    @Valid
    private List<VehicleItemRequest> items;
}
