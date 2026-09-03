package com.Car_Rental_API.module.master_data.province.dto;

import com.Car_Rental_API.module.master_data.province.repository.*;
import com.Car_Rental_API.module.master_data.province.mapper.*;
import com.Car_Rental_API.module.master_data.province.service.*;
import com.Car_Rental_API.module.master_data.province.model.*;
import com.Car_Rental_API.module.master_data.province.dto.*;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProvinceRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String googleMapUrl;

    private String description;
}
