package com.evcharging.bookingservice.client;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * Mints short-lived tokens booking-service uses to call the read-only "get by id" endpoints on
 * user-service/provider-service (signed with the same shared secret those services trust). Those
 * endpoints only require {@code authenticated()}, not a specific role, so an "entityType": "SERVICE"
 * token is accepted without needing to impersonate a real user or provider. Never expose this token
 * outside server-to-server calls.
 */
@Component
public class InternalServiceTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final long TTL_MILLIS = 60_000L;

    public String mintServiceToken() {
        Date now = new Date();
        Key signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .setSubject("booking-service")
                .addClaims(Map.of("entityType", "SERVICE", "entityId", 0L))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + TTL_MILLIS))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }
}
