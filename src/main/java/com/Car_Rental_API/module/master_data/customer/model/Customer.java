package com.Car_Rental_API.module.master_data.customer.model;

import com.Car_Rental_API.module.master_data.customer.repository.*;
import com.Car_Rental_API.module.master_data.customer.mapper.*;
import com.Car_Rental_API.module.master_data.customer.service.*;
import com.Car_Rental_API.module.master_data.customer.model.*;
import com.Car_Rental_API.module.master_data.customer.dto.*;

import com.Car_Rental_API.common.base.BaseAuditor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Customer extends BaseAuditor {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;
    private String fileName;
    private String fileUrl;
    private Byte isVerified;
    private Byte osType;
    private Byte language;
    private String deviceToken;
    private LocalDateTime lastLogin;
}
