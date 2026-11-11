package com.autota.server.domain.auth.service;

import com.autota.server.domain.auth.dto.LoginResponse;
import com.autota.server.domain.user.dto.UserCredential;
import com.autota.server.domain.auth.jwt.JwtProvider;
import com.autota.server.domain.user.service.UserService;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthService {
    /** bcrypt는 비밀번호를 72바이트까지만 쓴다. 넘는 값은 일치로 보지 않는다. */
    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    /** 없는 이메일에도 해시 비교를 한 번 하게 해서 응답 시간으로 계정 존재를 알 수 없게 한다. */
    private final String dummyHash;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.dummyHash = passwordEncoder.encode("timing-guard");
    }

    /** 이메일·비밀번호 중 무엇이 틀렸는지 구분하지 않고 AUTH-003으로 응답한다. 역할은 DB의 users.role을 쓴다. */
    public LoginResponse login(String email, String password) {
        Optional<UserCredential> found = userService.findCredentialByEmail(email);

        boolean passwordMatches = passwordMatches(password, found.map(UserCredential::passwordHash).orElse(dummyHash));
        if (found.isEmpty() || !passwordMatches) {
            log.warn("로그인 실패: email={}, reason={}", email, failureReason(found, passwordMatches));
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        UserCredential user = found.get();
        String token = jwtProvider.createAccessToken(user.id(), user.role());

        return new LoginResponse(
                token,
                jwtProvider.getExpiresInSeconds(),
                new LoginResponse.UserInfo(
                        user.id(),
                        user.name(),
                        user.email(),
                        user.role(),
                        user.studentNo()
                )
        );
    }

    private boolean passwordMatches(String rawPassword, String hash) {
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            passwordEncoder.matches("", hash); // 시간 차를 줄이려고 비교만 하고 결과는 버린다
            return false;
        }
        return passwordEncoder.matches(rawPassword, hash);
    }

    private String failureReason(Optional<UserCredential> found, boolean passwordMatches) {
        if (found.isEmpty()) return "없는 계정";
        return "비밀번호 불일치";
    }
}
