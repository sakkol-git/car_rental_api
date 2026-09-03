package com.Car_Rental_API.security.authorization.role.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class GroupRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private List<UserList> userList;

    private List<ModuleList> moduleList;

    @Data
    public static class UserList {
        private Long userId;
    }

    @Data
    public static class ModuleList {
        private Long moduleId;
    }
}
