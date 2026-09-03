package com.Car_Rental_API.module.master_data.customer_support.dto;

import com.Car_Rental_API.module.master_data.customer_support.repository.*;
import com.Car_Rental_API.module.master_data.customer_support.mapper.*;
import com.Car_Rental_API.module.master_data.customer_support.service.*;
import com.Car_Rental_API.module.master_data.customer_support.model.*;
import com.Car_Rental_API.module.master_data.customer_support.dto.*;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerSupportRequest {

    @NotBlank(message = "Name in Khmer is required")
    private String nameKh;

    @NotBlank(message = "Name in English is required")
    private String nameEn;

    private String nameZh;

    @NotNull(message = "Type of Contact is required")
    @Min(value = 1, message = "Type of Contact must be between 1 and 5")
    @Max(value = 5, message = "Type of Contact must be between 1 and 5")
    private Byte contactType;

    private String phoneNumber;
    private String link;
    private String fileName;
    private String fileUrl;

    @NotNull(message = "Order is required")
    private Integer sortOrder;
}
