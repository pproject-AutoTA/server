package com.autota.server.global.security;

import com.autota.server.auth.jwt.AuthUser;
import com.autota.server.auth.jwt.JwtProvider;
import com.autota.server.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.autota.server.global.security.RestAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE;

/** Bearer 토큰을 검사해 AuthUser(id, role)를 인증 정보로 올린다. 실패 사유는 요청 속성에 남기고 EntryPoint가 응답한다. */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                AuthUser user = jwtProvider.getAuthUser(header.substring(7));
                var auth = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute(AUTH_ERROR_ATTRIBUTE, ErrorCode.AUTH_002);
                log.debug("만료된 토큰: {}", request.getRequestURI());
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute(AUTH_ERROR_ATTRIBUTE, ErrorCode.AUTH_001);
                log.debug("유효하지 않은 토큰: {} ({})", request.getRequestURI(), e.getClass().getSimpleName());
            }
        }
        chain.doFilter(request, response);
    }
}
