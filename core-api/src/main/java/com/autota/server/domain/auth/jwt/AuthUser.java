package com.autota.server.domain.auth.jwt;

import com.autota.server.domain.user.entity.Role;

/** 토큰에서 꺼낸 로그인 사용자. 컨트롤러에서 @AuthenticationPrincipal 로 받는다. */
public record AuthUser(Long id, Role role) {

    public boolean is(Role role) {
        return this.role == role;
    }
}
