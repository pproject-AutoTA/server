package com.autota.server.auth.service;

import com.autota.server.auth.dto.LoginResponse;
import com.autota.server.auth.entity.Role;
import com.autota.server.auth.entity.User;
import com.autota.server.auth.jwt.JwtProvider;
import com.autota.server.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponse login(String username, String password, Role expectedRole) {
        User user = userRepository.findByUsername(username).orElseThrow(this::invalidCredentials);
        boolean passwordMatches = passwordEncoder.matches(password, user.getPasswordHash());
        boolean roleMatches = user.getRole() == expectedRole;

        if (!passwordMatches || !roleMatches) {
            throw invalidCredentials();
        }

        String token = jwtProvider.createAccessToken(user.getId(), user.getRole());

        return new LoginResponse(
                token,
                jwtProvider.getExpiresInSeconds(),
                new LoginResponse.UserInfo(
                        user.getId(),
                        user.getUsername(),
                        user.getRole(),
                        user.getStudentNo()
                )
        );
    }

    private BadCredentialsException invalidCredentials() {
        return new BadCredentialsException("유저 정보가 올바르지 않습니다.");
    } //에러는 나중에 따로 빼서 처리하도록
}
