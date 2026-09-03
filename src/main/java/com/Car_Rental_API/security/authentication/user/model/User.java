package com.Car_Rental_API.security.authentication.user.model;

import com.Car_Rental_API.common.base.BaseAuditor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseAuditor {
    // * Core Identification
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String firstName;
    private String lastName;
    private String photo;
    private String signature;

    // * Session & Authentication
    private String phoneVerifyToken;
    private String sessionId;
    private LocalDateTime sessionStart;
    private LocalDateTime sessionActive;
    private String sessionLat;
    private String sessionLong;
    private String refreshToken;
    private LocalDateTime refreshTokenExpiration;
    private String pushToken;
    private String deviceName;

    // * Login Tracking
    private String loginAttemptRemoteIp;
    private String loginAttemptHttpUserAgent;
    private Integer statusLogin; //
}
