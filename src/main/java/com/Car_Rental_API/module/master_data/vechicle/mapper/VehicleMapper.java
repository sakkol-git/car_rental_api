package com.Car_Rental_API.module.master_data.vechicle.mapper;

import com.Car_Rental_API.module.master_data.vechicle.repository.*;
import com.Car_Rental_API.module.master_data.vechicle.mapper.*;
import com.Car_Rental_API.module.master_data.vechicle.service.*;
import com.Car_Rental_API.module.master_data.vechicle.model.*;
import com.Car_Rental_API.module.master_data.vechicle.dto.*;


import com.Car_Rental_API.module.master_data.vechicle.model.Vehicle;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleRequest;
import com.Car_Rental_API.module.master_data.vechicle.dto.VehicleResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    VehicleResponse toResponse(Vehicle vehicle);

    List<VehicleResponse> toResponses(List<Vehicle> vehicles);

    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "isPublic", expression = "java(request.getIsPublic() != null ? request.getIsPublic() : 1L)")
    Vehicle fromCreateRequest(VehicleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "isPublic", expression = "java(request.getIsPublic() != null ? request.getIsPublic() : 1L)")
    void updateFromRequest(VehicleRequest request, @MappingTarget Vehicle vehicle);
}
