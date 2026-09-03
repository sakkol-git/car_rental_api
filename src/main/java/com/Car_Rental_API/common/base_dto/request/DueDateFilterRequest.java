package com.Car_Rental_API.common.base_dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DueDateFilterRequest extends BaseFilterRequest {
    private String dateFrom;
    private String dateTo;
}
