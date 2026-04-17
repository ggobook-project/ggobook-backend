package com.untitled.ggobook.util;

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

    private final Key key;
    // Access Token은 15분 (900,000 밀리초)으로 짧게 설정!
    private final long accessTokenExpTime = 900000;
    // Refresh Token은 7일 (604,800,000 밀리초)로 길게 설정!
    private final long refreshTokenExpTime = 604800000;

    public JwtUtil(@Value("${jwt.secret:this-is-my-super-secret-key-for-jwt-token-test-2026!@#}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 1. Access Token 발급 (기존과 동일, 수명만 15분)
    public String generateAccessToken(String userId, String role) {
        return createToken(userId, role, accessTokenExpTime);
    }

    // 2. 🌟 새로 추가됨: Refresh Token 발급 (수명 7일)
    public String generateRefreshToken(String userId, String role) {
        return createToken(userId, role, refreshTokenExpTime);
    }

    // 토큰 생성 중복 코드 제거 (Clean Code)
    private String createToken(String userId, String role, long expireTime) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("role", role);
        Date now = new Date();
        Date validity = new Date(now.getTime() + expireTime);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public String getRoleFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false; // 만료되거나 위조되면 false 반환
        }
    }
}