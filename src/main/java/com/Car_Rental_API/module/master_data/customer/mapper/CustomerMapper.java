package com.Car_Rental_API.module.master_data.customer.mapper;

import com.Car_Rental_API.module.master_data.customer.repository.*;
import com.Car_Rental_API.module.master_data.customer.mapper.*;
import com.Car_Rental_API.module.master_data.customer.service.*;
import com.Car_Rental_API.module.master_data.customer.model.*;
import com.Car_Rental_API.module.master_data.customer.dto.*;


import com.Car_Rental_API.module.master_data.customer.dto.CustomerRequest;
import com.Car_Rental_API.module.master_data.customer.dto.CustomerResponse;
import com.Car_Rental_API.module.master_data.customer.model.Customer;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    CustomerResponse toResponse(Customer customer);

    List<CustomerResponse> toResponses(List<Customer> customers);

    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "isVerified", constant = "1")
    Customer fromCreateRequest(CustomerRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(CustomerRequest request, @MappingTarget Customer customer);
}