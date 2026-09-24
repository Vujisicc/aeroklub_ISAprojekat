package com.aeroklub.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long accessMs, refreshMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-exp-ms}") long accessMs,
                            @Value("${jwt.refresh-exp-ms}") long refreshMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessMs = accessMs;
        this.refreshMs = refreshMs;
    }

    public String createAccessToken(String username, String role) {
        return builder(username, accessMs).claim("role", role).claim("type", "access").compact();
    }

    public String createRefreshToken(String username, String jti) {
        return builder(username, refreshMs).id(jti).claim("type", "refresh").compact();
    }

    /** Throws JwtException / IllegalArgumentException when invalid or expired. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private JwtBuilder builder(String subject, long ttlMs) {
        Date now = new Date();
        return Jwts.builder().subject(subject).issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMs)).signWith(key);
    }
}