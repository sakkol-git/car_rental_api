package com.Car_Rental_API.module.master_data.privacy_term.dto;

import com.Car_Rental_API.module.master_data.privacy_term.repository.*;
import com.Car_Rental_API.module.master_data.privacy_term.mapper.*;
import com.Car_Rental_API.module.master_data.privacy_term.service.*;
import com.Car_Rental_API.module.master_data.privacy_term.model.*;
import com.Car_Rental_API.module.master_data.privacy_term.dto.*;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrivacyTermRequest {

    @NotNull(message = "Type is required")
    @Min(value = 1, message = "Type must be Privacy or Terms")
    @Max(value = 2, message = "Type must be Privacy or Terms")
    private Long type;

    @NotBlank(message = "Description in Khmer is required")
    private String descriptionKh;

    @NotBlank(message = "Description in English is required")
    private String descriptionEn;

    private String descriptionZh;
}
