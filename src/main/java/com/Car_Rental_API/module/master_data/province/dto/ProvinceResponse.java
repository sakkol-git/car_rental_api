package com.Car_Rental_API.module.master_data.province.dto;

import com.Car_Rental_API.module.master_data.province.repository.*;
import com.Car_Rental_API.module.master_data.province.mapper.*;
import com.Car_Rental_API.module.master_data.province.service.*;
import com.Car_Rental_API.module.master_data.province.model.*;
import com.Car_Rental_API.module.master_data.province.dto.*;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProvinceResponse {
    private Long id;
    private String name;
    private String googleMapUrl;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modified;
    private String modifiedBy;
}
