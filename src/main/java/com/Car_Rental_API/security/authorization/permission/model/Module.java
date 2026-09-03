package com.Car_Rental_API.security.authorization.permission.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Module {

    private Long id;
    @JsonIgnore
    @JsonProperty("module_type_id")
    private Long moduleTypeId;
    private String name;
    private String type;
    private Boolean checked;
    @JsonIgnore
    private Integer ordering;
    @JsonIgnore
    private Integer status; // 0: disabled, 1: enabled
}