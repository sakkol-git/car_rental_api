package com.Car_Rental_API.module.master_data.customer_support.dto;

import com.Car_Rental_API.module.master_data.customer_support.repository.*;
import com.Car_Rental_API.module.master_data.customer_support.mapper.*;
import com.Car_Rental_API.module.master_data.customer_support.service.*;
import com.Car_Rental_API.module.master_data.customer_support.model.*;
import com.Car_Rental_API.module.master_data.customer_support.dto.*;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSupportResponse {
    private Long id;
    private String nameKh;
    private String nameEn;
    private String nameZh;
    private Byte contactType;
    private String phoneNumber;
    private String link;
    private String fileName;
    private String fileUrl;
    private Integer sortOrder;

    private LocalDateTime created;
    private String createdBy;
    private LocalDateTime modified;
    private String modifiedBy;
}
