package com.autota.server.global.exception;

import com.autota.server.global.common.ApiResponse;
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
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e, HttpServletRequest request) {
        return build(e.getErrorCode(), request);
    }

    /** @Valid 검증 실패: 필드별 사유를 errors로 내려준다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<ApiResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiResponse.FieldError(f.getField(), f.getRejectedValue(), f.getDefaultMessage()))
                .toList();
        return build(ErrorCode.COMMON_400, request, errors);
    }

    /** 경로 변수·쿼리 파라미터 타입이 맞지 않음 (예: /problems/abc) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        List<ApiResponse.FieldError> errors = List.of(
                new ApiResponse.FieldError(e.getName(), e.getValue(), "값의 형식이 올바르지 않습니다"));
        return build(ErrorCode.COMMON_400, request, errors);
    }

    /** @Validated 파라미터 검증 실패 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        List<ApiResponse.FieldError> errors = e.getConstraintViolations().stream()
                .map(v -> new ApiResponse.FieldError(v.getPropertyPath().toString(), v.getInvalidValue(), v.getMessage()))
                .toList();
        return build(ErrorCode.COMMON_400, request, errors);
    }

    /** 본문이 없거나 JSON 형식이 깨진 경우, 알 수 없는 enum 값 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        return build(ErrorCode.COMMON_400, request);
    }

    /** 컨트롤러 안(@PreAuthorize 등)에서 던져진 인가 실패는 500이 아니라 403으로 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        return build(ErrorCode.COMMON_403, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        return build(ErrorCode.COMMON_401, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e, HttpServletRequest request) {
        // 404, 405 등 스프링이 상태를 정해 둔 예외는 500으로 바꾸지 않고 그 상태를 유지한다.
        if (e instanceof ErrorResponse er) {
            return fromStatus(er.getStatusCode(), request);
        }
        log.error("처리되지 않은 예외: {} {}", request.getMethod(), request.getRequestURI(), e);
        return build(ErrorCode.COMMON_500, request);
    }

    private ResponseEntity<ApiResponse<Void>> fromStatus(HttpStatusCode status, HttpServletRequest request) {
        return switch (status.value()) {
            case 400 -> build(ErrorCode.COMMON_400, request);
            case 404 -> build(ErrorCode.COMMON_404, request);
            case 405 -> build(ErrorCode.COMMON_405, request);
            default -> ResponseEntity.status(status).body(
                    ApiResponse.fail("COMMON-" + status.value(), "요청을 처리할 수 없습니다", request.getRequestURI()));
        };
    }

    private ResponseEntity<ApiResponse<Void>> build(ErrorCode code, HttpServletRequest request) {
        return build(code, request, null);
    }

    private ResponseEntity<ApiResponse<Void>> build(ErrorCode code, HttpServletRequest request,
                                                    List<ApiResponse.FieldError> errors) {
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(code, request.getRequestURI(), errors));
    }
}
