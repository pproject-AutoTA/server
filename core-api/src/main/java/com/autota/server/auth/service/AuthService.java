package com.autota.server.auth.service;

import com.autota.server.auth.dto.LoginResponse;
import com.autota.server.auth.entity.Role;
import com.autota.server.auth.entity.User;
import com.autota.server.auth.jwt.JwtProvider;
import com.autota.server.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
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

    public LoginResponse login(String email, String password, Role expectedRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_003));
        boolean passwordMatches = passwordEncoder.matches(password, user.getPasswordHash());
        boolean roleMatches = user.getRole() == expectedRole;

        if (!passwordMatches || !roleMatches) {
            throw new BusinessException(ErrorCode.AUTH_003);
        }

        String token = jwtProvider.createAccessToken(user.getId(), user.getRole());

        return new LoginResponse(
                token,
                jwtProvider.getExpiresInSeconds(),
                new LoginResponse.UserInfo(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStudentNo()
                )
        );
    }
}
