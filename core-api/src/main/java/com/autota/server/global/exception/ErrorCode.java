package com.autota.server.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON-400", "입력값이 올바르지 않습니다"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "COMMON-401", "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-403", "권한이 없습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 오류입니다"),

    // 인증
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-001", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-002", "만료된 토큰입니다"),

    // 수업
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE-002", "참여 코드에 해당하는 수업이 없습니다"),
    ALREADY_ENROLLED(HttpStatus.CONFLICT, "COURSE-003", "이미 참여한 수업입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
