package com.Car_Rental_API.security.authorization.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleRequest {
    private String name;
    private List<ModuleDetail> modules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleDetail {
        private Long id;
        private String name;
        private String type;
    }
}

