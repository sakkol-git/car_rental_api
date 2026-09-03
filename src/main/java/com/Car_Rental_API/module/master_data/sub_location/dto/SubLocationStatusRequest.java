package com.Car_Rental_API.module.master_data.sub_location.dto;

import com.Car_Rental_API.module.master_data.sub_location.repository.*;
import com.Car_Rental_API.module.master_data.sub_location.mapper.*;
import com.Car_Rental_API.module.master_data.sub_location.service.*;
import com.Car_Rental_API.module.master_data.sub_location.model.*;
import com.Car_Rental_API.module.master_data.sub_location.dto.*;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubLocationStatusRequest {

    @NotNull(message = "Public status is required")
    @Min(value = 0, message = "Public status must be 0 or 1")
    @Max(value = 1, message = "Public status must be 0 or 1")
    private Byte isPublic;
}
