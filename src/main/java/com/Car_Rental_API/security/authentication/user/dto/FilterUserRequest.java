package com.Car_Rental_API.security.authentication.user.dto;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FilterUserRequest extends BaseFilterRequest {
    private Long groupId;
}

