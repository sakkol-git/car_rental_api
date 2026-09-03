package com.Car_Rental_API.module.master_data.vehicle_rental_type.mapper;

import com.Car_Rental_API.module.master_data.vehicle_rental_type.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.service.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.model.*;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.*;


import com.Car_Rental_API.module.master_data.vehicle_rental_type.model.VehicleRentalType;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeRequest;
import com.Car_Rental_API.module.master_data.vehicle_rental_type.dto.VehicleRentalTypeResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleRentalTypeMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    VehicleRentalTypeResponse toResponse(VehicleRentalType rentalType);

    List<VehicleRentalTypeResponse> toResponses(List<VehicleRentalType> rentalTypes);

    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "sortOrder", expression = "java(request.getSortOrder() != null ? request.getSortOrder() : 0)")
    VehicleRentalType fromCreateRequest(VehicleRentalTypeRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "sortOrder", expression = "java(request.getSortOrder() != null ? request.getSortOrder() : 0)")
    void updateFromRequest(VehicleRentalTypeRequest request, @MappingTarget VehicleRentalType rentalType);
}
