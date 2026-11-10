package com.autota.server.global.security;

import com.autota.server.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 401: 토큰 없음(COMMON-401) / 유효하지 않음(AUTH-001) / 만료(AUTH-002) */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /** 인증 필터가 토큰 검사 실패 사유(ErrorCode)를 남기는 요청 속성 이름 */
    public static final String AUTH_ERROR_ATTRIBUTE = "auth.error";

    private final SecurityErrorWriter writer;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        // 필터가 토큰 검사에 실패한 사유를 남겨 뒀으면 그것을, 아니면 토큰 없음으로 본다.
        Object reason = request.getAttribute(AUTH_ERROR_ATTRIBUTE);
        ErrorCode code = reason instanceof ErrorCode c ? c : ErrorCode.UNAUTHENTICATED;
        writer.write(request, response, code);
    }
}
