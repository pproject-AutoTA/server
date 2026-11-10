package com.autota.server.problem;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
        log.error("Unhandled exception: {}", request.getRequestURI(), e);
        return ResponseEntity.status(ErrorCode.COMMON_500.getStatus())
                .body(ApiResponse.fail(ErrorCode.COMMON_500, request.getRequestURI()));
    }
}
