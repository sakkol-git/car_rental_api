package com.Car_Rental_API.module.master_data.vehicle_category.mapper;

import com.Car_Rental_API.module.master_data.vehicle_category.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_category.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_category.service.*;
import com.Car_Rental_API.module.master_data.vehicle_category.model.*;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.*;


import com.Car_Rental_API.module.master_data.vehicle_category.model.VehicleCategory;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.VehicleCategoryRequest;
import com.Car_Rental_API.module.master_data.vehicle_category.dto.VehicleCategoryResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleCategoryMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    VehicleCategoryResponse toResponse(VehicleCategory category);

    List<VehicleCategoryResponse> toResponses(List<VehicleCategory> categories);

    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "sortOrder", expression = "java(request.getSortOrder() != null ? request.getSortOrder() : 0)")
    VehicleCategory fromCreateRequest(VehicleCategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "sortOrder", expression = "java(request.getSortOrder() != null ? request.getSortOrder() : 0)")
    void updateFromRequest(VehicleCategoryRequest request, @MappingTarget VehicleCategory category);
}
