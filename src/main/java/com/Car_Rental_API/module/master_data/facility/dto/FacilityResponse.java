package com.Car_Rental_API.module.master_data.facility.dto;

import com.Car_Rental_API.module.master_data.facility.repository.*;
import com.Car_Rental_API.module.master_data.facility.mapper.*;
import com.Car_Rental_API.module.master_data.facility.service.*;
import com.Car_Rental_API.module.master_data.facility.model.*;
import com.Car_Rental_API.module.master_data.facility.dto.*;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FacilityResponse {
    private Long id;
    private String nameKh;
    private String nameEn;
    private String nameZh;
    private String description;
    private String fileName;
    private String fileUrl;
    private Integer sortOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modified;
    private String modifiedBy;
}
