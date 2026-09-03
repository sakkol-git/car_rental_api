package com.Car_Rental_API.module.master_data.about_us.dto;

import com.Car_Rental_API.module.master_data.about_us.repository.*;
import com.Car_Rental_API.module.master_data.about_us.mapper.*;
import com.Car_Rental_API.module.master_data.about_us.service.*;
import com.Car_Rental_API.module.master_data.about_us.model.*;
import com.Car_Rental_API.module.master_data.about_us.dto.*;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AboutUsResponse {
    private Long id;
    private String descriptionKh;
    private String descriptionEn;
    private String descriptionZh;

    private LocalDateTime created;
    private String createdBy;
    private LocalDateTime modified;
    private String modifiedBy;
}
