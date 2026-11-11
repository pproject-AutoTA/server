package com.autota.server.domain.user.service;

import com.autota.server.domain.user.dto.UserCredential;
import com.autota.server.domain.user.entity.User;
import com.autota.server.domain.user.repository.UserRepository;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    /** 토큰은 유효한데 사용자가 없는 경우(삭제됨)는 인증 실패로 본다. */
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
    }

    /** 이메일은 입력값 그대로 조회한다. */
    public Optional<UserCredential> findCredentialByEmail(String email) {
        return userRepository.findByEmail(email).map(UserService::toCredential);
    }

    private static UserCredential toCredential(User user) {
        return new UserCredential(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStudentNo(),
                user.getPasswordHash()
        );
    }
}
