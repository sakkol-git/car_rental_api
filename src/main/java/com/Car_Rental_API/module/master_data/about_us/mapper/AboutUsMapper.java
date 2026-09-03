package com.Car_Rental_API.module.master_data.about_us.mapper;

import com.Car_Rental_API.module.master_data.about_us.repository.*;
import com.Car_Rental_API.module.master_data.about_us.mapper.*;
import com.Car_Rental_API.module.master_data.about_us.service.*;
import com.Car_Rental_API.module.master_data.about_us.model.*;
import com.Car_Rental_API.module.master_data.about_us.dto.*;


import com.Car_Rental_API.module.master_data.about_us.model.AboutUs;
import com.Car_Rental_API.module.master_data.about_us.dto.AboutUsRequest;
import com.Car_Rental_API.module.master_data.about_us.dto.AboutUsResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AboutUsMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    AboutUsResponse toResponse(AboutUs aboutUs);

    List<AboutUsResponse> toResponses(List<AboutUs> aboutUsList);

    @Mapping(target = "isActive", constant = "1")
    AboutUs fromCreateRequest(AboutUsRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(AboutUsRequest request, @MappingTarget AboutUs aboutUs);
}
