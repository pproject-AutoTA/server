package com.autota.server.global.config;

import com.autota.server.auth.jwt.JwtProvider;
import com.autota.server.global.security.JwtAuthenticationFilter;
import com.autota.server.global.security.RestAccessDeniedHandler;
import com.autota.server.global.security.RestAuthenticationEntryPoint;
import com.autota.server.auth.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // 학생 전용 API처럼 한 단계 더 좁은 검사는 컨트롤러의 @PreAuthorize로
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
                        // 그 외 API는 학생·교수 공통(관리자는 COMMON-403). 참여·소유 확인은 서비스에서 한다.
                        .requestMatchers("/api/v1/**").hasAnyRole(Role.STUDENT.name(), Role.PROFESSOR.name())
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
