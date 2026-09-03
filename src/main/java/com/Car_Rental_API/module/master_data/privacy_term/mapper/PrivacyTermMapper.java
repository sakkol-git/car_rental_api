package com.Car_Rental_API.module.master_data.privacy_term.mapper;

import com.Car_Rental_API.module.master_data.privacy_term.repository.*;
import com.Car_Rental_API.module.master_data.privacy_term.mapper.*;
import com.Car_Rental_API.module.master_data.privacy_term.service.*;
import com.Car_Rental_API.module.master_data.privacy_term.model.*;
import com.Car_Rental_API.module.master_data.privacy_term.dto.*;


import com.Car_Rental_API.module.master_data.privacy_term.model.PrivacyTerm;
import com.Car_Rental_API.module.master_data.privacy_term.dto.PrivacyTermRequest;
import com.Car_Rental_API.module.master_data.privacy_term.dto.PrivacyTermResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PrivacyTermMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    PrivacyTermResponse toResponse(PrivacyTerm privacyTerm);

    List<PrivacyTermResponse> toResponses(List<PrivacyTerm> privacyTerms);

    @Mapping(target = "isActive", constant = "1")
    PrivacyTerm fromCreateRequest(PrivacyTermRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(PrivacyTermRequest request, @MappingTarget PrivacyTerm privacyTerm);
}
