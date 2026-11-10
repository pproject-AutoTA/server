package com.autota.server.global.security;

import com.autota.server.auth.jwt.JwtProvider;
import com.autota.server.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String ERROR_ATTRIBUTE = "jwt.error";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtProvider.parse(header.substring(7));
                var auth = new UsernamePasswordAuthenticationToken(
                        Long.valueOf(claims.getSubject()),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class)))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute(ERROR_ATTRIBUTE, ErrorCode.AUTH_002);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute(ERROR_ATTRIBUTE, ErrorCode.AUTH_001);
            }
        }
        chain.doFilter(request, response);
    }
}
