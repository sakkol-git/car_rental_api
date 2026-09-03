package com.Car_Rental_API.security.authentication.util;

import java.util.List;
import java.util.Collections;
import java.util.function.Function;

import javax.crypto.SecretKey;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public long getJwtExpiration() { return jwtExpiration; }
    public long getRefreshTokenExpiration() { return refreshExpiration; }

    // * Token generation
    public String generateToken(Long userId, String username, List<String> groups) {
        return buildToken(userId, username, groups, jwtExpiration);
    }

    public String generateRefreshToken(Long userId, String username) {
        return buildToken(userId, username, Collections.emptyList(), refreshExpiration);
    }

    private String buildToken(Long userId, String username, List<String> groups, long expiration) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("groups", groups)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // * Token extraction
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Number id = extractClaim(token, claims -> claims.get("userId", Number.class));
        return id != null ? id.longValue() : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractGroups(String token) {
        return extractClaim(token, claims -> claims.get("groups", List.class));
    }

    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload());
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
