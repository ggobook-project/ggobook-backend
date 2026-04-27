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
    private final long refreshTokenExpTime = 54000000;

    public JwtUtil(@Value("${jwt.secret:this-is-my-super-secret-key-for-jwt-token-test-2026!@#}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 1. Access Token 발급 (String userId 제거, Long id 사용)
    public String generateAccessToken(Long id, String role) {
        return createToken(id, role, accessTokenExpTime);
    }

    // 2. Refresh Token 발급 (String userId 제거, Long id 사용)
    public String generateRefreshToken(Long id, String role) {
        return createToken(id, role, refreshTokenExpTime);
    }

    // 토큰 생성 코드 (PK와 Role만 구워 넣기)
    private String createToken(Long id, String role, long expireTime) {
        Claims claims = Jwts.claims();
        claims.put("id", id);
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

    // 토큰에서 PK(Long)를 꺼내는 메서드
    public Long getIdFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("id", Long.class);
    }

    // 토큰에서 Role(String)을 꺼내는 메서드
    public String getRoleFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}