package com.Car_Rental_API.module.master_data.journey_price.mapper;

import com.Car_Rental_API.module.master_data.journey_price.repository.*;
import com.Car_Rental_API.module.master_data.journey_price.mapper.*;
import com.Car_Rental_API.module.master_data.journey_price.service.*;
import com.Car_Rental_API.module.master_data.journey_price.model.*;
import com.Car_Rental_API.module.master_data.journey_price.dto.*;


import com.Car_Rental_API.module.master_data.journey_price.model.JourneyPrice;
import com.Car_Rental_API.module.master_data.journey_price.dto.JourneyPriceRequest;
import com.Car_Rental_API.module.master_data.journey_price.dto.JourneyPriceResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JourneyPriceMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    JourneyPriceResponse toResponse(JourneyPrice journeyPrice);

    List<JourneyPriceResponse> toResponses(List<JourneyPrice> journeyPrices);

    @Mapping(target = "isActive", constant = "1")
    JourneyPrice fromCreateRequest(JourneyPriceRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(JourneyPriceRequest request, @MappingTarget JourneyPrice journeyPrice);
}
