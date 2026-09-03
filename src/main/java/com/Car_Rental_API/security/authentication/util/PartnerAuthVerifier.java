package com.Car_Rental_API.security.authentication.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import com.Car_Rental_API.security.authentication.auth.dto.AuthUserResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "partner.datasource.url", matchIfMissing = false)
public class PartnerAuthVerifier {

    private final JdbcTemplate jdbcTemplate;

    public PartnerAuthVerifier(@Qualifier("partnerDataSource") DataSource partnerDataSource) {
        this.jdbcTemplate = new JdbcTemplate(partnerDataSource);
    }

    // * Authenticate partner user by credentials against user_logistics
    public Optional<AuthUserResponse> authenticateUser(String username, String rawPassword, PasswordEncoder passwordEncoder) {
        if (username == null || username.isBlank() || rawPassword == null) return Optional.empty();

        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(
                    "SELECT id, first_name, last_name, telephone, email, username, password FROM user_logistics WHERE (username = ? OR telephone = ? OR email = ?) AND status > 0 LIMIT 1",
                    username, username, username);
            if (list.isEmpty()) return Optional.empty();

            Map<String, Object> u = list.get(0);
            String dbPassword = str(u.get("password"));

            boolean matches = dbPassword != null && (
                    (passwordEncoder != null && dbPassword.startsWith("$2") && passwordEncoder.matches(rawPassword, dbPassword)) ||
                            rawPassword.equals(dbPassword) || md5(rawPassword).equalsIgnoreCase(dbPassword)
            );

            return matches ? Optional.of(buildUserResponse(u)) : Optional.empty();
        } catch (Exception e) {
            log.debug("Partner authentication failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // * Verify partner token via oauth_access_token and load user_logistics profile
    public Optional<AuthUserResponse> verify(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return Optional.empty();

        try {
            String token = rawToken.replaceAll("(?i)^(\\s*bearer\\s*)+", "").trim();
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT user_name FROM oauth_access_token WHERE token_id = ? OR token_id = ? LIMIT 1", md5(token), token);
            if (rows.isEmpty()) return Optional.empty();

            String username = str(rows.get(0).get("user_name"));
            return (username != null && !username.isBlank()) ? fetchUserByUsername(username) : Optional.empty();
        } catch (Exception e) {
            log.debug("Partner token verification failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // * Fetch user_logistics record by username/telephone/email
    private Optional<AuthUserResponse> fetchUserByUsername(String username) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList(
                "SELECT id, first_name, last_name, telephone, email, username FROM user_logistics WHERE (username = ? OR telephone = ? OR email = ?) AND status > 0 LIMIT 1",
                username, username, username);
        return list.isEmpty() ? Optional.empty() : Optional.of(buildUserResponse(list.get(0)));
    }

    // * Map DB row to AuthUserResponse
    private AuthUserResponse buildUserResponse(Map<String, Object> u) {
        String first = str(u.get("first_name")), last = str(u.get("last_name"));
        String fullName = (first != null ? first + (last != null ? " " + last : "") : (last != null ? last : str(u.get("username"))));

        AuthUserResponse user = new AuthUserResponse();
        user.setUserId(str(u.get("id")));
        user.setUsername(str(u.get("username")));
        user.setFullName(fullName);
        user.setPhone(str(u.get("telephone")));
        user.setEmail(str(u.get("email")));
        user.setOsType((byte) 1);
        return user;
    }

    // * MD5 hashing helper for Spring OAuth2 token_id
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String str(Object val) { return val != null ? val.toString() : null; }
}
