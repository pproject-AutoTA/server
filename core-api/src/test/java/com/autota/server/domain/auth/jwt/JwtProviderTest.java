package com.autota.server.domain.auth.jwt;

import com.autota.server.domain.auth.entity.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {
    private static final String SECRET = "test-secret-test-secret-test-secret-1234";

    @Test
    void 시크릿이_32바이트_미만이면_시작할_수_없다() {
        assertThatThrownBy(() -> new JwtProvider("too-short", 3600))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }

    @Test
    void 발급한_토큰에서_id와_역할을_꺼낸다() {
        JwtProvider jwt = new JwtProvider(SECRET, 3600);

        AuthUser user = jwt.getAuthUser(jwt.createAccessToken(42L, Role.PROFESSOR));

        assertThat(user).isEqualTo(new AuthUser(42L, Role.PROFESSOR));
        assertThat(user.is(Role.PROFESSOR)).isTrue();
        assertThat(jwt.getExpiresInSeconds()).isEqualTo(3600);
    }

    @Test
    void 만료된_토큰은_ExpiredJwtException() {
        JwtProvider expired = new JwtProvider(SECRET, -10);

        assertThatThrownBy(() -> expired.getAuthUser(expired.createAccessToken(1L, Role.STUDENT)))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void 다른_키로_서명된_토큰은_거부한다() {
        JwtProvider other = new JwtProvider("another-secret-another-secret-12345678", 3600);
        JwtProvider jwt = new JwtProvider(SECRET, 3600);

        assertThatThrownBy(() -> jwt.getAuthUser(other.createAccessToken(1L, Role.STUDENT)))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void role_claim이_없는_토큰은_거부한다() {
        JwtProvider jwt = new JwtProvider(SECRET, 3600);
        String noRole = Jwts.builder()
                .subject("1")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThatThrownBy(() -> jwt.getAuthUser(noRole)).isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void 알_수_없는_역할_값은_거부한다() {
        JwtProvider jwt = new JwtProvider(SECRET, 3600);
        String unknownRole = Jwts.builder()
                .subject("1")
                .claim("role", "SUPERUSER")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThatThrownBy(() -> jwt.getAuthUser(unknownRole)).isInstanceOf(IllegalArgumentException.class);
    }
}
