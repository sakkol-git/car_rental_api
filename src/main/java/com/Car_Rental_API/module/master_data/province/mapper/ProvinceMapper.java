package com.Car_Rental_API.module.master_data.province.mapper;

import com.Car_Rental_API.module.master_data.province.repository.*;
import com.Car_Rental_API.module.master_data.province.mapper.*;
import com.Car_Rental_API.module.master_data.province.service.*;
import com.Car_Rental_API.module.master_data.province.model.*;
import com.Car_Rental_API.module.master_data.province.dto.*;


import com.Car_Rental_API.module.master_data.province.model.Province;
import com.Car_Rental_API.module.master_data.province.dto.ProvinceRequest;
import com.Car_Rental_API.module.master_data.province.dto.ProvinceResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProvinceMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    ProvinceResponse toResponse(Province province);

    List<ProvinceResponse> toResponses(List<Province> provinces);

    @Mapping(target = "isActive", constant = "1")
    Province fromCreateRequest(ProvinceRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(ProvinceRequest request, @MappingTarget Province province);
}
