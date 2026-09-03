package com.Car_Rental_API.module.master_data.vechicle.dto;

import com.Car_Rental_API.module.master_data.vechicle.repository.*;
import com.Car_Rental_API.module.master_data.vechicle.mapper.*;
import com.Car_Rental_API.module.master_data.vechicle.service.*;
import com.Car_Rental_API.module.master_data.vechicle.model.*;
import com.Car_Rental_API.module.master_data.vechicle.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class VehicleFilterRequest extends BaseFilterRequest {
    private Long brandId;
    private Long modelId;
    private Long categoryId;
    private Long rentalTypeId;
    private Byte isPublic;
}
