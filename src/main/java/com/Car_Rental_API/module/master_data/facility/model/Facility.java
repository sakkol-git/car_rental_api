package com.Car_Rental_API.module.master_data.facility.model;

import com.Car_Rental_API.module.master_data.facility.repository.*;
import com.Car_Rental_API.module.master_data.facility.mapper.*;
import com.Car_Rental_API.module.master_data.facility.service.*;
import com.Car_Rental_API.module.master_data.facility.model.*;
import com.Car_Rental_API.module.master_data.facility.dto.*;


import com.Car_Rental_API.common.base.BaseAuditor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Facility extends BaseAuditor {

    private Long id;
    private String nameKh;
    private String nameEn;
    private String nameZh;
    private String description;
    private String fileName;
    private String fileUrl;
    private Integer sortOrder;
}
