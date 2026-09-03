package com.Car_Rental_API.security.authentication.auth.service;



import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.Car_Rental_API.security.authentication.auth.dto.AuthUserResponse;
import com.Car_Rental_API.security.authentication.auth.dto.LoginRequest;
import com.Car_Rental_API.security.authentication.auth.dto.LoginResponse;
import com.Car_Rental_API.security.authentication.auth.dto.RefreshTokenRequest;
import com.Car_Rental_API.security.authentication.user.model.User;
import com.Car_Rental_API.security.authentication.user.repository.UserRepository;
import com.Car_Rental_API.security.authentication.user.service.UserService;
import com.Car_Rental_API.common.exception.GlobalException;
import com.Car_Rental_API.security.authentication.util.JwtUtil;
import com.Car_Rental_API.security.authentication.util.PartnerAuthVerifier;
import com.Car_Rental_API.security.authorization.role.model.Group;
import com.Car_Rental_API.security.authorization.role.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final GroupService groupService;
    private final UserService userService;

    @Autowired(required = false)
    private PartnerAuthVerifier partnerAuthVerifier;

    @CircuitBreaker(name = "defaultService")
    public LoginResponse login(LoginRequest request) {
        // * 1. Try local system user authentication
        Optional<User> localUserOpt = userRepository.findByUsername(request.getUsername())
                .filter(u -> u.getIsActive() != null && u.getIsActive() == 1);

        if (localUserOpt.isPresent()) {
            User user = localUserOpt.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                List<String> groups = groupService.getUserGroups(user.getId()).stream()
                        .map(Group::getName).toList();

                String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), groups);
                String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
                LocalDateTime refreshTokenExpiration = LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getRefreshTokenExpiration()));

                // * Save refresh token and device info to database
                userRepository.updateRefreshToken(user.getId(), refreshToken, refreshTokenExpiration, request.getPushToken(), request.getDeviceName());

                // * Update session info
                String sid = UUID.randomUUID().toString();
                userService.updateUserSession(user.getId(), sid, LocalDateTime.now());

                return new LoginResponse(accessToken, refreshToken, "Bearer", jwtUtil.getJwtExpiration() / 1000);
            }
        }

        // * 2. Fallback to partner database (user_logistics in vet_logistic)
        if (partnerAuthVerifier != null) {
            Optional<AuthUserResponse> partnerUserOpt = partnerAuthVerifier.authenticateUser(request.getUsername(), request.getPassword(), passwordEncoder);
            if (partnerUserOpt.isPresent()) {
                AuthUserResponse pUser = partnerUserOpt.get();
                Long userId = Long.valueOf(pUser.getUserId());
                List<String> groups = List.of("PARTNER");

                String accessToken = jwtUtil.generateToken(userId, pUser.getUsername(), groups);
                String refreshToken = jwtUtil.generateRefreshToken(userId, pUser.getUsername());

                return new LoginResponse(accessToken, refreshToken, "Bearer", jwtUtil.getJwtExpiration() / 1000);
            }
        }

        throw new GlobalException("Invalid credentials", 401);
    }

    @CircuitBreaker(name = "defaultService")
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // * 1. Validate token structure and extract username
        String username = jwtUtil.extractUsername(refreshToken);
        if (username == null || jwtUtil.isTokenExpired(refreshToken)) {
            throw new GlobalException("Invalid or expired refresh token", 401);
        }

        // * 2. Check database for token presence and expiration
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new GlobalException("Refresh token not found", 401));

        if (user.getRefreshTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new GlobalException("Refresh token has expired", 401);
        }

        // * 3. Generate new tokens
        List<String> groups = groupService.getUserGroups(user.getId()).stream()
                .map(Group::getName).toList();

        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), groups);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
        LocalDateTime newRefreshTokenExpiration = LocalDateTime.now()
                .plus(java.time.Duration.ofMillis(jwtUtil.getRefreshTokenExpiration()));

        // * 4. Update database with new refresh token (preserving device info)
        userRepository.updateRefreshToken(user.getId(), newRefreshToken, newRefreshTokenExpiration, user.getPushToken(), user.getDeviceName());

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtUtil.getJwtExpiration() / 1000);
    }

    @CircuitBreaker(name = "defaultService")
    public User register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new GlobalException("Username already exists", 400);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreated(LocalDateTime.now());
        user.setIsActive(1);

        return userRepository.save(user, true);
    }

    // * User Logout
    public void logout(Long userId) {
        userService.updateUserSession(userId, null, null);
    }
}
