package com.autota.server.global.exception;

import com.autota.server.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e, HttpServletRequest request) {
        return build(e.getErrorCode(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("AUTH-401", e.getMessage(), request.getRequestURI()));
    }

    /** @Valid 검증 실패: 필드별 사유를 errors로 내려준다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<ApiResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiResponse.FieldError(f.getField(), f.getRejectedValue(), f.getDefaultMessage()))
                .toList();
        ErrorCode code = ErrorCode.INVALID_INPUT;
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(code.getCode(), code.getMessage(), request.getRequestURI(), errors));
    }

    /** 본문이 없거나 JSON 형식이 깨진 경우 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        return build(ErrorCode.INVALID_INPUT, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e, HttpServletRequest request) {
        // 404, 405 등 스프링이 상태를 정해 둔 예외는 그 상태를 유지한다.
        if (e instanceof ErrorResponse er) {
            HttpStatusCode status = er.getStatusCode();
            return ResponseEntity.status(status)
                    .body(ApiResponse.fail("COMMON-" + status.value(), "요청을 처리할 수 없습니다", request.getRequestURI()));
        }
        log.error("처리되지 않은 예외: {} {}", request.getMethod(), request.getRequestURI(), e);
        return build(ErrorCode.INTERNAL_ERROR, request);
    }

    private ResponseEntity<ApiResponse<Void>> build(ErrorCode code, HttpServletRequest request) {
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(code.getCode(), code.getMessage(), request.getRequestURI()));
    }
}
