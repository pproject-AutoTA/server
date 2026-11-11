package com.autota.server.domain.user.dto;

import com.autota.server.domain.user.entity.Role;

/** 로그인 검증에 필요한 사용자 정보. 엔티티 대신 다른 도메인에 넘긴다. */
public record UserCredential(
        Long id,
        String name,
        String email,
        Role role,
        String studentNo,
        String passwordHash
) {
    /** 로그에 찍혀도 해시가 새지 않게 뺀다. */
    @Override
    public String toString() {
        return "UserCredential[id=" + id + ", email=" + email + ", role=" + role + "]";
    }
}
