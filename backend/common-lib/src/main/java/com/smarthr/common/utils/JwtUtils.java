package com.smarthr.common.utils;

import java.security.PublicKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtUtils {

    public Claims extractAllClaims(String token, PublicKey publicKey) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token, PublicKey publicKey) {
        return extractAllClaims(token, publicKey).getSubject();
    }

    public String extractRole(String token, PublicKey publicKey) {
        return extractAllClaims(token, publicKey).get("role", String.class);
    }

    public String extractJti(String token, PublicKey publicKey) {
        return extractAllClaims(token, publicKey).getId();
    }
}