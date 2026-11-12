package com.autota.server.domain.user.service;

import com.autota.server.domain.user.dto.UserCredential;
import com.autota.server.domain.user.entity.Role;
import com.autota.server.domain.user.entity.User;
import com.autota.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    /** 이메일은 입력값 그대로 조회한다. */
    public Optional<UserCredential> findCredentialByEmail(String email) {
        return userRepository.findByEmail(email).map(UserService::toCredential);
    }

    public Optional<String> findNameById(Long id) {
        return userRepository.findById(id).map(User::getName);
    }

    public Role findRoleById(Long id) {
        return userRepository.findRoleById(id);
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
