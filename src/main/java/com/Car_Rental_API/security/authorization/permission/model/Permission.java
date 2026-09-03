package com.Car_Rental_API.security.authorization.permission.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    private Long id;
    private Long groupId;
    private Long moduleId;
}
