package com.Car_Rental_API.security.authentication.auth.dto;


import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String pushToken;
    private String deviceName;
}
