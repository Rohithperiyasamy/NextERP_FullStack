package com.enterprise.workflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // ─── Generate a token for a user ──────────────────────────────
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)           // who this token belongs to
                .claim("role", role)         // store role inside token
                .setIssuedAt(new Date())     // when token was created
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ─── Get email from token ──────────────────────────────────────
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // ─── Get role from token ───────────────────────────────────────
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    // ─── Check if token is still valid ────────────────────────────
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token); // if this doesn't throw, token is valid
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Private helpers ──────────────────────────────────────────
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}