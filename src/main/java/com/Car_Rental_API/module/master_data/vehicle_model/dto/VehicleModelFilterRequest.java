package com.Car_Rental_API.module.master_data.vehicle_model.dto;

import com.Car_Rental_API.module.master_data.vehicle_model.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_model.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_model.service.*;
import com.Car_Rental_API.module.master_data.vehicle_model.model.*;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class VehicleModelFilterRequest extends BaseFilterRequest {
    private Long brandId;
}
