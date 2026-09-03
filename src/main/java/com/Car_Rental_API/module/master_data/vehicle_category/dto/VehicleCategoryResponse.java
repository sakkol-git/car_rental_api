package com.Car_Rental_API.module.master_data.vehicle_category.dto;

import com.Car_Rental_API.module.master_data.vehicle_category.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_category.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_category.service.*;
import com.Car_Rental_API.module.master_data.vehicle_category.model.*;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.*;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCategoryResponse {
    private Long id;
    private String nameKh;
    private String nameEn;
    private String nameZh;
    private String descriptionKh;
    private String descriptionEn;
    private String descriptionZh;
    private String fileName;
    private String fileUrl;
    private Integer sortOrder;
    
    private LocalDateTime created;
    private String createdBy;
    private LocalDateTime modified;
    private String modifiedBy;
}
