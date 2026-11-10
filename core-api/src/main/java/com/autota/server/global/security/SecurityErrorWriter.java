package com.autota.server.global.security;

import com.autota.server.global.common.ApiResponse;
import com.autota.server.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/** 시큐리티 필터 단계의 인증·인가 실패를 공통 응답 JSON으로 쓴다. Accept 헤더와 무관하게 JSON으로 응답한다. */
@Component
@RequiredArgsConstructor
public class SecurityErrorWriter {
    private final ObjectMapper objectMapper;

    public void write(HttpServletRequest request, HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> body = ApiResponse.fail(code, request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
