package com.autota.server.global.exception;

import com.autota.server.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(code, request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<ApiResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiResponse.FieldError(f.getField(), f.getRejectedValue(), f.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(ErrorCode.COMMON_400.getStatus())
                .body(ApiResponse.fail(ErrorCode.COMMON_400, request.getRequestURI(), errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        return ResponseEntity.status(ErrorCode.COMMON_400.getStatus())
                .body(ApiResponse.fail(ErrorCode.COMMON_400, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e, HttpServletRequest request) {
        // 404, 405 등 스프링이 상태를 정해 둔 예외는 500으로 바꾸지 않고 그 상태를 유지한다.
        if (e instanceof ErrorResponse er) {
            HttpStatusCode status = er.getStatusCode();
            return ResponseEntity.status(status)
                    .body(ApiResponse.fail("COMMON-" + status.value(), "요청을 처리할 수 없습니다", request.getRequestURI()));
        }
        log.error("Unhandled exception: {}", request.getRequestURI(), e);
        return ResponseEntity.status(ErrorCode.COMMON_500.getStatus())
                .body(ApiResponse.fail(ErrorCode.COMMON_500, request.getRequestURI()));
    }
}
