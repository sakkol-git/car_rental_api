package com.Car_Rental_API.security.authorization.permission.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleType {

    private Long id;
    private String name;
    @JsonIgnore
    private Integer ordering;
    @JsonIgnore
    private Integer status; // 0: disabled, 1: enabled
    private List<Module> moduleList;
}

