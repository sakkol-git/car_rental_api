package com.Car_Rental_API.security.authentication.user.mapper;

import java.util.List;
import java.util.Objects;

import com.Car_Rental_API.security.authentication.user.dto.*;
import com.Car_Rental_API.security.authentication.user.model.User;
import com.Car_Rental_API.security.authorization.permission.model.Module;
import com.Car_Rental_API.security.authorization.role.model.Group;
import com.Car_Rental_API.security.authorization.permission.model.ModuleType;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // * Map to full response
    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    UserResponse toResponse(User user);

    // * Map to list item response
    @Mapping(target = "createdBy", source = "createdByFullName")
    @Mapping(target = "modifiedBy", source = "modifiedByFullName")
    UserListResponse toListItem(User user);

    // * Create user entity from request
    @Mapping(target = "fullName", expression = "java(this.concatenateFullName(req))")
    @Mapping(target = "isActive", constant = "1")
    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    User fromCreateRequest(UserRequest req);

    // * Update existing user entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "fullName", expression = "java(this.concatenateFullName(req, user))")
    @Mapping(target = "modified", expression = "java(java.time.LocalDateTime.now())")
    void updateUserFromRequest(UserRequest req, @MappingTarget User user);

    // * Concatenate first and last name
    default String concatenateFullName(UserRequest req) {
        if (req == null) return null;
        return (Objects.toString(req.getFirstName(), "") + " " + Objects.toString(req.getLastName(), "")).trim();
    }

    default String concatenateFullName(UserRequest req, User user) {
        if (req == null || user == null) return user != null ? user.getFullName() : null;

        String first = (req.getFirstName() != null && !req.getFirstName().isBlank()) ? req.getFirstName() : Objects.toString(user.getFirstName(), "");
        String last = (req.getLastName() != null && !req.getLastName().isBlank()) ? req.getLastName() : Objects.toString(user.getLastName(), "");

        return (first + " " + last).trim();
    }

    // * Map Group model to RoleResponse DTO
    RoleSummary toRoleResponse(Group group);

    List<RoleSummary> toRoleResponses(List<Group> groups);

    // * Enrich module types for user profile
    default void enrichModulesForProfile(List<ModuleType> types, List<Module> modules) {
        if (types == null || modules == null) return;
        types.forEach(type -> {
            type.setModuleList(modules.stream().filter(m -> Objects.equals(m.getModuleTypeId(), type.getId())).peek(m -> {
                m.setType(m.getType());
                m.setChecked(true);
            }).toList());
        });
    }
}
