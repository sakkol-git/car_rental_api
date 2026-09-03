package com.Car_Rental_API.module.master_data.vehicle_brand.mapper;

import com.Car_Rental_API.module.master_data.vehicle_brand.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.service.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.model.*;
import com.Car_Rental_API.module.master_data.vehicle_brand.dto.*;


import com.Car_Rental_API.module.master_data.vehicle_brand.model.VehicleBrand;
import com.Car_Rental_API.module.master_data.vehicle_brand.dto.VehicleBrandRequest;
import com.Car_Rental_API.module.master_data.vehicle_brand.dto.VehicleBrandResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleBrandMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    VehicleBrandResponse toResponse(VehicleBrand brand);

    List<VehicleBrandResponse> toResponses(List<VehicleBrand> brands);

    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "sortOrder", expression = "java(request.getSortOrder() != null ? request.getSortOrder() : 0)")
    VehicleBrand fromCreateRequest(VehicleBrandRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "sortOrder", expression = "java(request.getSortOrder() != null ? request.getSortOrder() : 0)")
    void updateFromRequest(VehicleBrandRequest request, @MappingTarget VehicleBrand brand);
}
