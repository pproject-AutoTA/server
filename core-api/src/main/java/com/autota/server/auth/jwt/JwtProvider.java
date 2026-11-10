package com.autota.server.auth.jwt;

import com.autota.server.auth.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {
    private final SecretKey key;
    private final long expiresInSeconds;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expires-in-seconds}") long expiresInSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiresInSeconds = expiresInSeconds;
    }

    public String createAccessToken(Long userId, Role role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiresInSeconds * 1000))
                .signWith(key)
                .compact();
    }

    /** 유효하지 않으면 JwtException 발생. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    /** 토큰에서 사용자 id를 꺼낸다. 유효하지 않으면 JwtException. */
    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    /** 토큰에서 역할을 꺼낸다. 유효하지 않으면 JwtException. */
    public Role getRole(String token) {
        return Role.valueOf(parse(token).get("role", String.class));
    }

    /** 토큰을 한 번만 파싱해 id와 역할을 함께 꺼낸다. */
    public AuthUser getAuthUser(String token) {
        Claims claims = parse(token);
        return new AuthUser(
                Long.valueOf(claims.getSubject()),
                Role.valueOf(claims.get("role", String.class))
        );
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
