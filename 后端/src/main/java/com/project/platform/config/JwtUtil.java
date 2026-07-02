package com.project.platform.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    // 密钥（长度必须 >=32 位）
    private static final String SECRET_KEY = "platform-admin-token-202504abcdefghijklmn";
    private static final long EXPIRATION = 1 * 24 * 60 * 60 * 1000L; // 1天

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 生成Token
    public String generateToken(Integer adminId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("username", username);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    // 解析Token
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
}