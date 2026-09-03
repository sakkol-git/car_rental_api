package com.Car_Rental_API.security.authentication.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserResponse {
    private String userId;
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private Byte osType;
}