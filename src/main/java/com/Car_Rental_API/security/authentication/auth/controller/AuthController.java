package com.Car_Rental_API.security.authentication.auth.controller;

import com.Car_Rental_API.security.authentication.auth.service.AuthService;
import com.Car_Rental_API.security.authentication.auth.dto.LoginRequest;
import com.Car_Rental_API.security.authentication.auth.dto.LoginResponse;
import com.Car_Rental_API.security.authentication.auth.dto.RefreshTokenRequest;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.security.authentication.util.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "01. Authentication", description = "APIs for Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User Login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(BaseResponse.response(authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Access Token")
    public ResponseEntity<BaseResponse<LoginResponse>> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(BaseResponse.response(authService.refreshToken(request)));
    }

    // @PostMapping("/register")
    // @Operation(summary = "User Register", description = "Register new user (Public)")
    // public ResponseEntity<BaseResponse<User>> register(@RequestBody User user) {
    // 	return ResponseEntity.ok(BaseResponse.success(authService.register(user)));
    // }

    // * User Logout
    @PostMapping("/logout")
    @Operation(summary = "User Logout")
    public ResponseEntity<BaseResponse<Object>> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) authService.logout(userDetails.getUserId());
        return ResponseEntity.ok(BaseResponse.response(null));
    }
}
