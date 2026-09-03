package com.Car_Rental_API.module.master_data.vechicle.model;

import com.Car_Rental_API.module.master_data.vechicle.repository.*;
import com.Car_Rental_API.module.master_data.vechicle.mapper.*;
import com.Car_Rental_API.module.master_data.vechicle.service.*;
import com.Car_Rental_API.module.master_data.vechicle.model.*;
import com.Car_Rental_API.module.master_data.vechicle.dto.*;


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
public class Vehicle extends BaseAuditor {

    private Long id;
    private Long brandId;
    private Long modelId;
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
    private String brandName;
    private String modelName;
}
