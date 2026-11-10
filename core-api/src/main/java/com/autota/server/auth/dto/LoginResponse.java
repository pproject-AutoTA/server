package com.autota.server.auth.dto;

import com.autota.server.auth.entity.Role;
import com.fasterxml.jackson.annotation.JsonInclude;

public record LoginResponse(
        String accessToken,
        long expiresIn,
        UserInfo user
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record UserInfo(
            Long id,
            String email,
            Role role,
            String studentNo
    ) {}
}
