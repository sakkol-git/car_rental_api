package com.Car_Rental_API.module.master_data.vehicle_rental_type.model;

import com.Car_Rental_API.module.master_data.vehicle_rental_type.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.service.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.model.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.*;


import com.Car_Rental_API.common.base.BaseAuditor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRentalType extends BaseAuditor {

    private Long id;
    private Long categoryId;
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
    private String categoryNameKh;
    private String categoryNameEn;
    private String categoryNameZh;
}
