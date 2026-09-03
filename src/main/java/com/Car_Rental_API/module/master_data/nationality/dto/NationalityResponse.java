package com.Car_Rental_API.module.master_data.nationality.dto;

import com.Car_Rental_API.module.master_data.nationality.repository.*;
import com.Car_Rental_API.module.master_data.nationality.mapper.*;
import com.Car_Rental_API.module.master_data.nationality.service.*;
import com.Car_Rental_API.module.master_data.nationality.model.*;
import com.Car_Rental_API.module.master_data.nationality.dto.*;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NationalityResponse {
    private Long id;
    private String nameKh;
    private String nameEn;
    private String nameZh;
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
