package com.Car_Rental_API.module.master_data.facility.dto;

import com.Car_Rental_API.module.master_data.facility.repository.*;
import com.Car_Rental_API.module.master_data.facility.mapper.*;
import com.Car_Rental_API.module.master_data.facility.service.*;
import com.Car_Rental_API.module.master_data.facility.model.*;
import com.Car_Rental_API.module.master_data.facility.dto.*;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FacilityRequest {

    @NotBlank(message = "Name in Khmer is required")
    private String nameKh;

    @NotBlank(message = "Name in English is required")
    private String nameEn;

    private String nameZh;
    private String description;
    private String fileName;

    @NotBlank(message = "Icon is required")
    private String fileUrl;

    private Integer sortOrder;
}
