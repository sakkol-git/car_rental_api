package com.Car_Rental_API.module.master_data.privacy_term.dto;

import com.Car_Rental_API.module.master_data.privacy_term.repository.*;
import com.Car_Rental_API.module.master_data.privacy_term.mapper.*;
import com.Car_Rental_API.module.master_data.privacy_term.service.*;
import com.Car_Rental_API.module.master_data.privacy_term.model.*;
import com.Car_Rental_API.module.master_data.privacy_term.dto.*;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivacyTermResponse {
    private Long id;
    private Long type;
    private String descriptionKh;
    private String descriptionEn;
    private String descriptionZh;

    private LocalDateTime created;
    private String createdBy;
    private LocalDateTime modified;
    private String modifiedBy;
}
