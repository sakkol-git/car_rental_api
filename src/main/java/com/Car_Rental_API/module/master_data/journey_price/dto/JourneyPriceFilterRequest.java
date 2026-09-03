package com.Car_Rental_API.module.master_data.journey_price.dto;

import com.Car_Rental_API.module.master_data.journey_price.repository.*;
import com.Car_Rental_API.module.master_data.journey_price.mapper.*;
import com.Car_Rental_API.module.master_data.journey_price.service.*;
import com.Car_Rental_API.module.master_data.journey_price.model.*;
import com.Car_Rental_API.module.master_data.journey_price.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class JourneyPriceFilterRequest extends BaseFilterRequest {
    private Long fromProvinceId;
    private Long toProvinceId;
    private Long vehicleModelId;
}
