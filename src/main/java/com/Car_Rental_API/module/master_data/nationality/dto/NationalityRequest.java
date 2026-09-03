package com.Car_Rental_API.module.master_data.nationality.dto;

import com.Car_Rental_API.module.master_data.nationality.repository.*;
import com.Car_Rental_API.module.master_data.nationality.mapper.*;
import com.Car_Rental_API.module.master_data.nationality.service.*;
import com.Car_Rental_API.module.master_data.nationality.model.*;
import com.Car_Rental_API.module.master_data.nationality.dto.*;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NationalityRequest {

    @NotBlank(message = "Name in Khmer is required")
    private String nameKh;

    @NotBlank(message = "Name in English is required")
    private String nameEn;

    private String nameZh;
    private String fileName;

    @NotBlank(message = "Icon is required")
    private String fileUrl;

    private Integer sortOrder;
}
