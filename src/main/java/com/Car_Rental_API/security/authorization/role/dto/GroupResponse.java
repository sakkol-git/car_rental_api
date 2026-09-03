package com.Car_Rental_API.security.authorization.role.dto;

import com.Car_Rental_API.common.base_dto.response.DropdownResponse;
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
public class GroupResponse {
    private Long id;
    private String name;
    private List<DropdownResponse> users;
    private LocalDateTime created;
    private String createdBy;
    private LocalDateTime modified;
    private String modifiedBy;
    private List<ModuleType> moduleTypeList;
}
