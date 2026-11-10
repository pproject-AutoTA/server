package com.autota.server.auth.dto;

import com.autota.server.auth.entity.Role;

public record LoginResponse(
        String accessToken,
        long expiresIn,
        UserInfo user
) {
    public record UserInfo(
            Long id,
            String name,
            String email,
            Role role,
            String studentNo // 학생이 아니면 null (필드는 항상 내려간다)
    ) {}
}
