package com.Car_Rental_API.module.master_data.customer.dto;

import com.Car_Rental_API.module.master_data.customer.repository.*;
import com.Car_Rental_API.module.master_data.customer.mapper.*;
import com.Car_Rental_API.module.master_data.customer.service.*;
import com.Car_Rental_API.module.master_data.customer.model.*;
import com.Car_Rental_API.module.master_data.customer.dto.*;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {
    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String phoneNumber;

    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 255, message = "File name must not exceed 255 characters")
    private String fileName;

    @Size(max = 255, message = "File URL must not exceed 255 characters")
    private String fileUrl;

    private Byte osType;
    private Byte language;

    @Size(max = 65535, message = "Device token must not exceed 65535 characters")
    private String deviceToken;
}
