package com.Car_Rental_API.module.master_data.sub_location.dto;

import com.Car_Rental_API.module.master_data.sub_location.repository.*;
import com.Car_Rental_API.module.master_data.sub_location.mapper.*;
import com.Car_Rental_API.module.master_data.sub_location.service.*;
import com.Car_Rental_API.module.master_data.sub_location.model.*;
import com.Car_Rental_API.module.master_data.sub_location.dto.*;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubLocationRequest {

    @NotNull(message = "Province is required")
    private Long provinceId;

    @NotBlank(message = "Location Name is required")
    private String name;

    @NotNull(message = "Default Price is required")
    @DecimalMin(value = "0.00", message = "Default Price must be >= 0")
    private BigDecimal defaultPrice;

    private String googleMapUrl;
    private String description;

    @Min(value = 0, message = "Public status must be 0 or 1")
    @Max(value = 1, message = "Public status must be 0 or 1")
    private Byte isPublic;
}
