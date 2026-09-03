package com.Car_Rental_API.module.master_data.nationality.mapper;

import com.Car_Rental_API.module.master_data.nationality.repository.*;
import com.Car_Rental_API.module.master_data.nationality.mapper.*;
import com.Car_Rental_API.module.master_data.nationality.service.*;
import com.Car_Rental_API.module.master_data.nationality.model.*;
import com.Car_Rental_API.module.master_data.nationality.dto.*;


import com.Car_Rental_API.module.master_data.nationality.model.Nationality;
import com.Car_Rental_API.module.master_data.nationality.dto.NationalityRequest;
import com.Car_Rental_API.module.master_data.nationality.dto.NationalityResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NationalityMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    NationalityResponse toResponse(Nationality nationality);

    List<NationalityResponse> toResponses(List<Nationality> nationalities);

    @Mapping(target = "isActive", constant = "1")
    Nationality fromCreateRequest(NationalityRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(NationalityRequest request, @MappingTarget Nationality nationality);
}
