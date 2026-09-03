package com.Car_Rental_API.security.authorization.role.mapper;


import com.Car_Rental_API.security.authorization.role.dto.GroupRequest;
import com.Car_Rental_API.security.authorization.role.dto.GroupResponse;
import com.Car_Rental_API.security.authorization.role.model.Group;
import org.mapstruct.*;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GroupMapper {

    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    @Mapping(target = "users", ignore = true)
    GroupResponse toResponse(Group group);

    List<GroupResponse> toResponses(List<Group> groups);

    @Mapping(target = "isActive", constant = "1")
    Group fromCreateRequest(GroupRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(GroupRequest req, @MappingTarget Group group);
}

