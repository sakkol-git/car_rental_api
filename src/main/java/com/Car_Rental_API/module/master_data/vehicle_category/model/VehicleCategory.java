package com.Car_Rental_API.module.master_data.vehicle_category.model;

import com.Car_Rental_API.module.master_data.vehicle_category.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_category.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_category.service.*;
import com.Car_Rental_API.module.master_data.vehicle_category.model.*;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.*;


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
public class VehicleCategory extends BaseAuditor {

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

}
