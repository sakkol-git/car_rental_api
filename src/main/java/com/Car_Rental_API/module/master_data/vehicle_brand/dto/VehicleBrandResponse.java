package com.Car_Rental_API.module.master_data.vehicle_brand.dto;

import com.Car_Rental_API.module.master_data.vehicle_brand.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.service.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.model.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.dto.*;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleBrandResponse {
    private Long id;
    private String name;
    private String description;
    private String fileName;
    private String fileUrl;
    private Integer sortOrder;

    private LocalDateTime created;
    private String createdBy;
    private LocalDateTime modified;
    private String modifiedBy;
}
