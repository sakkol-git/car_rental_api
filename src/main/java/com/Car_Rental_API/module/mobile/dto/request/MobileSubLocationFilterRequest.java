package com.Car_Rental_API.module.mobile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MobileSubLocationFilterRequest {

    @Schema(description = "Origin province ID (used for journey route pricing lookup)")
    private Long fromProvinceId;

    @Schema(description = "Destination province ID (used for filtering sub-locations and route pricing lookup)")
    private Long toProvinceId;

    @Schema(description = "Vehicle model ID for journey pricing lookup")
    private Long vehicleModelId;

    @Schema(description = "Journey trip type (1: One-Way, 2: One-Day-Tour, 3: Round-Trip, 4: Multi-City, 5: City Tour)", example = "1")
    private Byte journeyType;
}
