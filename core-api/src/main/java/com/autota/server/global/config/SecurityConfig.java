package com.autota.server.global.config;

import com.autota.server.domain.auth.jwt.JwtProvider;
import com.autota.server.global.security.JwtAuthenticationFilter;
import com.autota.server.global.security.RestAccessDeniedHandler;
import com.autota.server.global.security.RestAuthenticationEntryPoint;
import com.autota.server.domain.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // CorsConfig의 CorsConfigurationSource 사용
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/v1/professor/**").hasRole(Role.PROFESSOR.name())
                        .requestMatchers("/api/v1/admin/**").hasRole(Role.ADMIN.name())
                        // 학생 전용 API는 경로를 하나씩 지정한다. /problems/** 처럼 묶으면
                        // 나중에 같은 경로 아래 공통 API가 생겼을 때 교수가 막힌다.
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/enrollments",
                                "/api/v1/problems/*/submissions").hasRole(Role.STUDENT.name())
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/problems/*",
                                "/api/v1/problems/*/submissions/me",
                                "/api/v1/submissions/me/*").hasRole(Role.STUDENT.name())
                        // 그 외는 학생·교수 공통(관리자는 COMMON-403). 참여·소유 확인은 서비스에서 한다.
                        .anyRequest().hasAnyRole(Role.STUDENT.name(), Role.PROFESSOR.name()))
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
