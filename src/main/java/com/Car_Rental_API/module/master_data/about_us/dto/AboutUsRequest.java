package com.Car_Rental_API.module.master_data.about_us.dto;

import com.Car_Rental_API.module.master_data.about_us.repository.*;
import com.Car_Rental_API.module.master_data.about_us.mapper.*;
import com.Car_Rental_API.module.master_data.about_us.service.*;
import com.Car_Rental_API.module.master_data.about_us.model.*;
import com.Car_Rental_API.module.master_data.about_us.dto.*;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AboutUsRequest {

    @NotBlank(message = "Description in Khmer is required")
    private String descriptionKh;

    @NotBlank(message = "Description in English is required")
    private String descriptionEn;

    private String descriptionZh;
}
