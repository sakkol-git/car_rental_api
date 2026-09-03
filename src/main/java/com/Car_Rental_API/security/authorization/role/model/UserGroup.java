package com.Car_Rental_API.security.authorization.role.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroup {

    private Long id;
    private Long userId;
    private Long groupId;
}
