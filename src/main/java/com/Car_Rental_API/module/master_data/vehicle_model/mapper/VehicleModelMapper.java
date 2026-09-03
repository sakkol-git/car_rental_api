package com.Car_Rental_API.module.master_data.vehicle_model.mapper;

import com.Car_Rental_API.module.master_data.vehicle_model.repository.*;
import com.Car_Rental_API.module.master_data.vehicle_model.mapper.*;
import com.Car_Rental_API.module.master_data.vehicle_model.service.*;
import com.Car_Rental_API.module.master_data.vehicle_model.model.*;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.*;


import com.Car_Rental_API.module.master_data.vehicle_model.model.VehicleModel;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.VehicleModelRequest;
import com.Car_Rental_API.module.master_data.vehicle_model.dto.VehicleModelResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleModelMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    VehicleModelResponse toResponse(VehicleModel model);

    List<VehicleModelResponse> toResponses(List<VehicleModel> models);

    @Mapping(target = "isActive", constant = "1")
    VehicleModel fromCreateRequest(VehicleModelRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(VehicleModelRequest request, @MappingTarget VehicleModel model);
}
