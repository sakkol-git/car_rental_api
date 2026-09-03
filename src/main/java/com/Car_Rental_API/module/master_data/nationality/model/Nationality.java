package com.Car_Rental_API.module.master_data.nationality.model;

import com.Car_Rental_API.module.master_data.nationality.repository.*;
import com.Car_Rental_API.module.master_data.nationality.mapper.*;
import com.Car_Rental_API.module.master_data.nationality.service.*;
import com.Car_Rental_API.module.master_data.nationality.model.*;
import com.Car_Rental_API.module.master_data.nationality.dto.*;


import com.Car_Rental_API.common.base.BaseAuditor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Nationality extends BaseAuditor {

    private Long id;
    private String nameKh;
    private String nameEn;
    private String nameZh;
    private String fileName;
    private String fileUrl;
    private Integer sortOrder;
}
