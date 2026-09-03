package com.Car_Rental_API.security.authentication.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String oldPassword;

    @NotBlank(message = "New password cannot be empty")
    private String newPassword;

}
