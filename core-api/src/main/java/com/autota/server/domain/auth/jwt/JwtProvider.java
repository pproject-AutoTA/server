package com.autota.server.domain.auth.jwt;

import com.autota.server.domain.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final long expiresInSeconds;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expires-in-seconds}") long expiresInSeconds
    ) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("jwt.secret(JWT_SECRET)은 " + MIN_SECRET_BYTES + "바이트 이상이어야 합니다");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
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

    /**
     * 토큰을 한 번만 파싱해 id와 역할을 함께 꺼낸다.
     * 서명·만료 문제는 JwtException, 역할이 없거나 알 수 없는 값이면 JwtException/IllegalArgumentException.
     */
    public AuthUser getAuthUser(String token) {
        Claims claims = parse(token);
        String role = claims.get("role", String.class);
        if (role == null) {
            throw new MalformedJwtException("role claim이 없습니다");
        }
        return new AuthUser(Long.valueOf(claims.getSubject()), Role.valueOf(role));
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}
