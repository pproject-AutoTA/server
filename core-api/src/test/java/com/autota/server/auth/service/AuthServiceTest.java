package com.autota.server.auth.service;

import com.autota.server.auth.dto.LoginResponse;
import com.autota.server.auth.entity.Role;
import com.autota.server.auth.entity.User;
import com.autota.server.auth.jwt.AuthUser;
import com.autota.server.auth.jwt.JwtProvider;
import com.autota.server.auth.repository.UserRepository;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private static final String SECRET = "test-secret-test-secret-test-secret-1234";

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder hasher = new BCryptPasswordEncoder(4); // 테스트라 비용을 낮춤. 사용자 해시 생성용
    private final PasswordEncoder encoder = spy(new BCryptPasswordEncoder(4)); // 서비스에 주입(호출 검증용)
    private final JwtProvider jwt = new JwtProvider(SECRET, 3600);
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, encoder, jwt);
    }

    @Test
    void 로그인에_성공하면_토큰과_사용자_정보를_돌려준다() {
        when(userRepository.findByEmail("kim@univ.ac.kr"))
                .thenReturn(Optional.of(user(7L, "kim@univ.ac.kr", "pw1234", Role.STUDENT, "20240001")));

        LoginResponse res = authService.login("kim@univ.ac.kr", "pw1234", Role.STUDENT);

        assertThat(res.expiresIn()).isEqualTo(3600);
        assertThat(res.user().id()).isEqualTo(7L);
        assertThat(res.user().name()).isEqualTo("김학생");
        assertThat(res.user().email()).isEqualTo("kim@univ.ac.kr");
        assertThat(res.user().role()).isEqualTo(Role.STUDENT);
        assertThat(res.user().studentNo()).isEqualTo("20240001");
        AuthUser fromToken = jwt.getAuthUser(res.accessToken());
        assertThat(fromToken).isEqualTo(new AuthUser(7L, Role.STUDENT));
    }

    @Test
    void 이메일은_공백과_대소문자를_정규화해서_조회한다() {
        when(userRepository.findByEmail("kim@univ.ac.kr"))
                .thenReturn(Optional.of(user(7L, "kim@univ.ac.kr", "pw1234", Role.STUDENT, null)));

        LoginResponse res = authService.login("  Kim@Univ.AC.kr ", "pw1234", Role.STUDENT);

        assertThat(res.user().id()).isEqualTo(7L);
        verify(userRepository).findByEmail("kim@univ.ac.kr");
    }

    @Test
    void 없는_이메일은_AUTH_003이고_해시_비교를_한_번_한다() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("none@univ.ac.kr", "pw1234", Role.STUDENT))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_003));
        // 계정 존재 여부가 응답 시간으로 드러나지 않도록 없는 이메일도 비교를 거친다
        verify(encoder).matches(anyString(), anyString());
    }

    @Test
    void 비밀번호가_틀리면_AUTH_003() {
        when(userRepository.findByEmail("kim@univ.ac.kr"))
                .thenReturn(Optional.of(user(7L, "kim@univ.ac.kr", "pw1234", Role.STUDENT, null)));

        assertThatThrownBy(() -> authService.login("kim@univ.ac.kr", "wrong", Role.STUDENT))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_003));
    }

    @Test
    void 역할이_다르면_AUTH_003() {
        when(userRepository.findByEmail("kim@univ.ac.kr"))
                .thenReturn(Optional.of(user(7L, "kim@univ.ac.kr", "pw1234", Role.STUDENT, null)));

        assertThatThrownBy(() -> authService.login("kim@univ.ac.kr", "pw1234", Role.PROFESSOR))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_003));
    }

    @Test
    void 비밀번호가_72바이트를_넘으면_AUTH_003() {
        String longPassword = "a".repeat(73);
        when(userRepository.findByEmail("kim@univ.ac.kr"))
                .thenReturn(Optional.of(user(7L, "kim@univ.ac.kr", longPassword, Role.STUDENT, null)));

        assertThatThrownBy(() -> authService.login("kim@univ.ac.kr", longPassword, Role.STUDENT))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_003));
    }

    private User user(Long id, String email, String rawPassword, Role role, String studentNo) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "name", "김학생");
        ReflectionTestUtils.setField(user, "email", email);
        ReflectionTestUtils.setField(user, "role", role);
        ReflectionTestUtils.setField(user, "studentNo", studentNo);
        String hashable = rawPassword.length() > 72 ? rawPassword.substring(0, 72) : rawPassword;
        ReflectionTestUtils.setField(user, "passwordHash", hasher.encode(hashable));
        return user;
    }
}
