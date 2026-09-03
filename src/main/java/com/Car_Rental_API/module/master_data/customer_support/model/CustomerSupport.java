package com.Car_Rental_API.module.master_data.customer_support.model;

import com.Car_Rental_API.module.master_data.customer_support.repository.*;
import com.Car_Rental_API.module.master_data.customer_support.mapper.*;
import com.Car_Rental_API.module.master_data.customer_support.service.*;
import com.Car_Rental_API.module.master_data.customer_support.model.*;
import com.Car_Rental_API.module.master_data.customer_support.dto.*;


import com.Car_Rental_API.common.base.BaseAuditor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSupport extends BaseAuditor {

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
}
