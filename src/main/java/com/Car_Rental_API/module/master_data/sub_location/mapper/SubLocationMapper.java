package com.Car_Rental_API.module.master_data.sub_location.mapper;

import com.Car_Rental_API.module.master_data.sub_location.repository.*;
import com.Car_Rental_API.module.master_data.sub_location.mapper.*;
import com.Car_Rental_API.module.master_data.sub_location.service.*;
import com.Car_Rental_API.module.master_data.sub_location.model.*;
import com.Car_Rental_API.module.master_data.sub_location.dto.*;


import com.Car_Rental_API.module.master_data.sub_location.model.SubLocation;
import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationRequest;
import com.Car_Rental_API.module.master_data.sub_location.dto.SubLocationResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubLocationMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    SubLocationResponse toResponse(SubLocation subLocation);

    List<SubLocationResponse> toResponses(List<SubLocation> subLocations);

    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "defaultPrice", expression = "java(request.getDefaultPrice() != null ? request.getDefaultPrice() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "isPublic", expression = "java(request.getIsPublic() != null ? request.getIsPublic() : (byte) 1)")
    SubLocation fromCreateRequest(SubLocationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "defaultPrice", expression = "java(request.getDefaultPrice() != null ? request.getDefaultPrice() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "isPublic", expression = "java(request.getIsPublic() != null ? request.getIsPublic() : (byte) 1)")
    void updateFromRequest(SubLocationRequest request, @MappingTarget SubLocation subLocation);
}
