package com.Car_Rental_API.common.base_dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BaseFilterRequest {

    @Min(value = 1, message = "page must be >= 1")
    private int page = 1;

    @Min(value = 1, message = "size must be >= 1")
    @Max(value = 100, message = "size must be <= 100")
    private int size = 10;

    private String keyword;
    private String sortBy;
}