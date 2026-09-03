package com.Car_Rental_API.module.master_data.facility.mapper;

import com.Car_Rental_API.module.master_data.facility.repository.*;
import com.Car_Rental_API.module.master_data.facility.mapper.*;
import com.Car_Rental_API.module.master_data.facility.service.*;
import com.Car_Rental_API.module.master_data.facility.model.*;
import com.Car_Rental_API.module.master_data.facility.dto.*;


import com.Car_Rental_API.module.master_data.facility.model.Facility;
import com.Car_Rental_API.module.master_data.facility.dto.FacilityRequest;
import com.Car_Rental_API.module.master_data.facility.dto.FacilityResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FacilityMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    FacilityResponse toResponse(Facility facility);

    List<FacilityResponse> toResponses(List<Facility> facilities);

    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "sortOrder", expression = "java(request.getSortOrder() != null ? request.getSortOrder() : 0)")
    Facility fromCreateRequest(FacilityRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "sortOrder", expression = "java(request.getSortOrder() != null ? request.getSortOrder() : 0)")
    void updateFromRequest(FacilityRequest request, @MappingTarget Facility facility);
}
