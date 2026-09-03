package com.Car_Rental_API.module.master_data.vehicle_rental_type.dto;

import com.Car_Rental_API.module.master_data.vehicle_rental_type.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.service.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.model.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.*;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRentalTypeResponse {
    private Long id;
    private Long categoryId;
    private String categoryNameKh;
    private String categoryNameEn;
    private String categoryNameZh;
    private String nameKh;
    private String nameEn;
    private String nameZh;
    private String googleMapUrl;
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
