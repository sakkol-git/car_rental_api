package com.Car_Rental_API.module.master_data.customer_support.mapper;

import com.Car_Rental_API.module.master_data.customer_support.repository.*;
import com.Car_Rental_API.module.master_data.customer_support.mapper.*;
import com.Car_Rental_API.module.master_data.customer_support.service.*;
import com.Car_Rental_API.module.master_data.customer_support.model.*;
import com.Car_Rental_API.module.master_data.customer_support.dto.*;


import com.Car_Rental_API.module.master_data.customer_support.model.CustomerSupport;
import com.Car_Rental_API.module.master_data.customer_support.dto.CustomerSupportRequest;
import com.Car_Rental_API.module.master_data.customer_support.dto.CustomerSupportResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerSupportMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    CustomerSupportResponse toResponse(CustomerSupport customerSupport);

    List<CustomerSupportResponse> toResponses(List<CustomerSupport> customerSupports);

    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "sortOrder", expression = "java(request.getSortOrder() != null ? request.getSortOrder() : 0)")
    CustomerSupport fromCreateRequest(CustomerSupportRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "sortOrder", expression = "java(request.getSortOrder() != null ? request.getSortOrder() : 0)")
    void updateFromRequest(CustomerSupportRequest request, @MappingTarget CustomerSupport customerSupport);
}
