package com.Car_Rental_API.module.master_data.vechicle.dto;

import com.Car_Rental_API.module.master_data.vechicle.repository.*;
import com.Car_Rental_API.module.master_data.vechicle.mapper.*;
import com.Car_Rental_API.module.master_data.vechicle.service.*;
import com.Car_Rental_API.module.master_data.vechicle.model.*;
import com.Car_Rental_API.module.master_data.vechicle.dto.*;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleItemResponse {
    private Long id;
    private String code;
    private String plateNumber;
    private Integer status;
    private LocalDateTime created;
    private LocalDateTime modified;
}
