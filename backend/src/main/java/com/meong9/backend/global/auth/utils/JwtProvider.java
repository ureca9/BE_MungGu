package com.meong9.backend.global.auth.utils;

import com.meong9.backend.global.exception.AuthenticationException;
import com.meong9.backend.global.utils.RoleCodeMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;

@Slf4j(topic = "JwtProvider")
@Component
public class JwtProvider {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "Refresh-token";

    private static final String BEARER_PREFIX = "Bearer ";

    public long JWT_TOKEN_EXPIRATION_TIME = 60 * 60 * 1000L * 5; // 5시간
    public long REFRESH_TOKEN_EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L; // 1주일

    @Value("${jwt.token.secretKey}")
    private String SECRET_KEY;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String getTokenFromRequest(HttpServletRequest request, String headerName) {
        String bearerToken = request.getHeader(headerName);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


    public String createAccessToken(String email, String roleCode) {
        String role = RoleCodeMapper.getRole(roleCode);
        return BEARER_PREFIX + Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_EXPIRATION_TIME))
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String email, String roleCode) {
        String role = RoleCodeMapper.getRole(roleCode);

        return BEARER_PREFIX + Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setExpiration(new Date(System.currentTimeMillis() + (REFRESH_TOKEN_EXPIRATION_TIME)))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public void validateToken(String token) {
        try {
            // 1. Bearer 제거 및 트리밍
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key) // 서명 키 설정
                    .build()
                    .parseClaimsJws(token) // JWT 검증 및 Claims 추출
                    .getBody();

        } catch (JwtException | IllegalArgumentException e) {
            throw AuthenticationException.unauthenticatedToken(token);
        }
    }

    public String getSubjectFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    public Claims getClaims(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
