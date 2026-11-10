package com.autota.server.auth.service;

import com.autota.server.auth.entity.User;
import com.autota.server.auth.repository.UserRepository;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 다른 도메인이 사용자 정보가 필요할 때 부르는 창구. UserRepository를 직접 쓰지 않는다. */
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
}
