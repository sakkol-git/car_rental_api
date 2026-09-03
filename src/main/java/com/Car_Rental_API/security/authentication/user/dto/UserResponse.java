package com.Car_Rental_API.security.authentication.user.dto;

import com.Car_Rental_API.security.authorization.permission.model.ModuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private Long employeeId;
    private String username;
    private String fullName;
    private String firstName;
    private String lastName;
    private String photo;
    private String signature;
    private List<RoleSummary> roles;
    private Integer isActive;
    private LocalDateTime created;
    private String createdBy;
    private LocalDateTime modified;
    private String modifiedBy;
    private List<ModuleType> moduleTypeList;
}