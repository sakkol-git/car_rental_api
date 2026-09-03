package com.Car_Rental_API.module.master_data.vechicle.dto;

import com.Car_Rental_API.module.master_data.vechicle.repository.*;
import com.Car_Rental_API.module.master_data.vechicle.mapper.*;
import com.Car_Rental_API.module.master_data.vechicle.service.*;
import com.Car_Rental_API.module.master_data.vechicle.model.*;
import com.Car_Rental_API.module.master_data.vechicle.dto.*;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private Long id;
    private Long brandId;
    private String brandName;
    private Long modelId;
    private String modelName;
    private String nameKh;
    private String nameEn;
    private String nameZh;
    private String vehicleCode;
    private String plateNumber;
    private String fileName;
    private String fileUrl;
    private Integer quantity;
    private Integer passengers;
    private Long isPublic;
    private Double averageRating;

    private List<VehicleCategoryItemResponse> categories;
    private List<VehicleRentalTypeItemResponse> rentalTypes;
    private List<VehicleSlideResponse> slides;
    private List<VehicleFacilityResponse> facilities;
    private List<VehicleItemResponse> items;

    private LocalDateTime created;
    private String createdBy;
    private LocalDateTime modified;
    private String modifiedBy;
}
