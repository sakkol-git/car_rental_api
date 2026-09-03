package com.Car_Rental_API.security.authentication.filter;


import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.security.authentication.util.CustomUserDetails;
import com.Car_Rental_API.security.authentication.util.JwtUtil;
import com.Car_Rental_API.security.authentication.util.PartnerAuthVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_URLS = List.of("/swagger-ui.html", "/swagger-ui", "/v3/api-docs", "/api-docs", "/swagger-resources", "/webjars", "/auth/login", "/auth/register", "/actuator", "/uploads", "/upload.html", "/mobile", "/payment", "/dropdown");
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    @Autowired(required = false)
    private PartnerAuthVerifier partnerAuthVerifier;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // * Skip public URLs
        if (PUBLIC_URLS.stream().anyMatch(request.getServletPath()::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            sendError(response, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header", request);
            return;
        }

        String token = authHeader.replaceAll("(?i)^(\\s*bearer\\s*)+", "").trim();
        boolean authenticated = false;

        // * 1. Verify local system JWT
        try {
            String username = jwtUtil.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null && jwtUtil.isTokenValid(token, username)) {
                Long userId = jwtUtil.extractUserId(token);
                List<String> groups = jwtUtil.extractGroups(token);
                setSecurityContext(new CustomUserDetails(userId, username, "", groups != null ? groups : Collections.emptyList(), true), request);
                authenticated = true;
            }
        } catch (Exception e) {
            log.debug("Local JWT verification skipped: {}", e.getMessage());
        }

        // * 2. Fallback to Partner token verification (vet_logistic / user_logistics)
        if (!authenticated && SecurityContextHolder.getContext().getAuthentication() == null && partnerAuthVerifier != null) {
            try {
                var partnerOpt = partnerAuthVerifier.verify(token);
                if (partnerOpt.isPresent()) {
                    var p = partnerOpt.get();
                    Long userId = p.getUserId() != null ? Long.valueOf(p.getUserId()) : 0L;
                    setSecurityContext(new CustomUserDetails(userId, p.getUsername(), "", List.of("PARTNER"), true, true, p.getFullName(), p.getEmail(), p.getPhone()), request);
                    authenticated = true;
                }
            } catch (Exception e) {
                log.debug("Partner token verification failed: {}", e.getMessage());
            }
        }

        if (authenticated) {
            filterChain.doFilter(request, response);
        } else {
            SecurityContextHolder.clearContext();
            sendError(response, HttpStatus.UNAUTHORIZED, "Token verification failed: Invalid or unverified token", request);
        }
    }

    // * Set Spring Security Context
    private void setSecurityContext(CustomUserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    // * Send JSON error response
    private void sendError(HttpServletResponse response, HttpStatus status, String message, HttpServletRequest request) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(BaseResponse.error(status, message)));
    }
}



