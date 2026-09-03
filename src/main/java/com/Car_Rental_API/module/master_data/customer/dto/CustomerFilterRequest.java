package com.Car_Rental_API.module.master_data.customer.dto;

import com.Car_Rental_API.module.master_data.customer.repository.*;
import com.Car_Rental_API.module.master_data.customer.mapper.*;
import com.Car_Rental_API.module.master_data.customer.service.*;
import com.Car_Rental_API.module.master_data.customer.model.*;
import com.Car_Rental_API.module.master_data.customer.dto.*;



import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CustomerFilterRequest extends BaseFilterRequest {
    private Byte isVerified;
    private Byte osType;
}
