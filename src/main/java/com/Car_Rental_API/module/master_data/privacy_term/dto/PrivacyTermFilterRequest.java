package com.Car_Rental_API.module.master_data.privacy_term.dto;

import com.Car_Rental_API.module.master_data.privacy_term.repository.*;
import com.Car_Rental_API.module.master_data.privacy_term.mapper.*;
import com.Car_Rental_API.module.master_data.privacy_term.service.*;
import com.Car_Rental_API.module.master_data.privacy_term.model.*;
import com.Car_Rental_API.module.master_data.privacy_term.dto.*;


import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PrivacyTermFilterRequest extends BaseFilterRequest {
    private Long type;
}
