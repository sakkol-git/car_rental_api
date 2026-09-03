package com.Car_Rental_API.module.master_data.vehicle_model.model;

import com.Car_Rental_API.module.master_data.vehicle_model.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_model.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_model.service.*;
import com.Car_Rental_API.module.master_data.vehicle_model.model.*;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.*;


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
public class VehicleModel extends BaseAuditor {

    private Long id;
    private Long brandId;
    private String brandName;
    private String name;
    private String description;
}
