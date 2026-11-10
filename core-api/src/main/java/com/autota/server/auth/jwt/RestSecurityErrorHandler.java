package com.autota.server.auth.jwt;

import com.autota.server.problem.BusinessException;
import com.autota.server.problem.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/** 인증/인가 실패를 GlobalExceptionHandler로 넘겨 공통 응답 형식(JSON)으로 내려준다. */
@Component
public class RestSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final HandlerExceptionResolver resolver;

    public RestSecurityErrorHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) {
        ErrorCode code = request.getAttribute(JwtAuthenticationFilter.ERROR_ATTRIBUTE) instanceof ErrorCode c
                ? c : ErrorCode.COMMON_401;
        resolver.resolveException(request, response, null, new BusinessException(code));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) {
        resolver.resolveException(request, response, null, new BusinessException(ErrorCode.COMMON_403));
    }
}
