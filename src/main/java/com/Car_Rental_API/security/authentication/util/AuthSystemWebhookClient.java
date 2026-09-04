package com.Car_Rental_API.security.authentication.util;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


import com.Car_Rental_API.security.authentication.auth.dto.AuthUserResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthSystemWebhookClient {

	private final JwtUtil jwtUtil;

	@Autowired(required = false)
	private PartnerAuthVerifier partnerAuthVerifier;

	// * Verify token via Partner DB, local JWT, or SecurityContext fallback
	public Optional<AuthUserResponse> verifyAndGetUserInfo(String rawToken) {
		if (rawToken != null && !rawToken.isBlank()) {
			String token = rawToken.replaceAll("(?i)^(\\s*bearer\\s*)+", "").trim();

			// * 1. Partner cross-platform token verification
			if (partnerAuthVerifier != null) {
				Optional<AuthUserResponse> pbResult = partnerAuthVerifier.verify(token);
				if (pbResult.isPresent()) return pbResult;
			}

			// * 2. Local JWT parsing
			try {
				String username = jwtUtil.extractUsername(token);
				Long userId = jwtUtil.extractUserId(token);
				if (username != null && !jwtUtil.isTokenExpired(token)) {
					AuthUserResponse user = new AuthUserResponse();
					user.setUserId(userId != null ? String.valueOf(userId) : null);
					user.setUsername(username);
					user.setPhone(username);
					user.setFullName(username);
					user.setOsType((byte) 1);
					return Optional.of(user);
				}
			} catch (Exception e) {
				log.debug("Failed to parse JWT token: {}", e.getMessage());
			}
		}

		// * 3. SecurityContext fallback for authenticated requests
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof CustomUserDetails u) {
			AuthUserResponse user = new AuthUserResponse();
			user.setUserId(u.getUserId() != null ? String.valueOf(u.getUserId()) : null);
			user.setUsername(u.getUsername());
			user.setFullName(u.getFullName());
			user.setPhone(u.getPhone() != null ? u.getPhone() : u.getUsername());
			user.setEmail(u.getEmail());
			user.setOsType((byte) 1);
			return Optional.of(user);
		}

		return Optional.empty();
	}
}
