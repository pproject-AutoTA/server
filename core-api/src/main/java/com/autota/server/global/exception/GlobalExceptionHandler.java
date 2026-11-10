package com.autota.server.global.exception;

import com.autota.server.global.common.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException e, HttpServletRequest request) {
        return build(e.getErrorCode(), request);
    }

    /** @Valid 검증 실패: 필드별 사유를 errors로 내려준다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<ApiErrorResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiErrorResponse.FieldError(f.getField(), f.getRejectedValue(), f.getDefaultMessage()))
                .toList();
        return build(ErrorCode.INVALID_INPUT, request, errors);
    }

    /** 경로 변수·쿼리 파라미터 타입이 맞지 않음 (예: /problems/abc) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        List<ApiErrorResponse.FieldError> errors = List.of(
                new ApiErrorResponse.FieldError(e.getName(), e.getValue(), "값의 형식이 올바르지 않습니다"));
        return build(ErrorCode.INVALID_INPUT, request, errors);
    }

    /** @Validated 파라미터 검증 실패 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        List<ApiErrorResponse.FieldError> errors = e.getConstraintViolations().stream()
                .map(v -> new ApiErrorResponse.FieldError(v.getPropertyPath().toString(), v.getInvalidValue(), v.getMessage()))
                .toList();
        return build(ErrorCode.INVALID_INPUT, request, errors);
    }

    /** 본문이 없거나 JSON 형식이 깨진 경우, 알 수 없는 enum 값 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        return build(ErrorCode.INVALID_INPUT, request);
    }

    /** 컨트롤러 안(@PreAuthorize 등)에서 던져진 인가 실패는 500이 아니라 403으로 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        return build(ErrorCode.FORBIDDEN, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        return build(ErrorCode.UNAUTHENTICATED, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception e, HttpServletRequest request) {
        // 404, 405 등 스프링이 상태를 정해 둔 예외는 500으로 바꾸지 않고 그 상태를 유지한다.
        if (e instanceof ErrorResponse er) {
            return fromStatus(er.getStatusCode(), request);
        }
        log.error("처리되지 않은 예외: {} {}", request.getMethod(), request.getRequestURI(), e);
        return build(ErrorCode.INTERNAL_ERROR, request);
    }

    private ResponseEntity<ApiErrorResponse> fromStatus(HttpStatusCode status, HttpServletRequest request) {
        return switch (status.value()) {
            case 400 -> build(ErrorCode.INVALID_INPUT, request);
            case 404 -> build(ErrorCode.NOT_FOUND, request);
            case 405 -> build(ErrorCode.METHOD_NOT_ALLOWED, request);
            default -> ResponseEntity.status(status).body(
                    ApiErrorResponse.of("COMMON-" + status.value(), "요청을 처리할 수 없습니다", request.getRequestURI()));
        };
    }

    private ResponseEntity<ApiErrorResponse> build(ErrorCode code, HttpServletRequest request) {
        return build(code, request, null);
    }

    private ResponseEntity<ApiErrorResponse> build(ErrorCode code, HttpServletRequest request,
                                                    List<ApiErrorResponse.FieldError> errors) {
        return ResponseEntity.status(code.getStatus())
                .body(ApiErrorResponse.of(code, request.getRequestURI(), errors));
    }
}
