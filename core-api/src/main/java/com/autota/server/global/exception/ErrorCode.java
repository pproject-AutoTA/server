package com.autota.server.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON-400", "입력값이 올바르지 않습니다"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "COMMON-401", "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-403", "권한이 없습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 오류입니다"),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-001", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-002", "만료된 토큰입니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-003", "이메일 또는 비밀번호가 올바르지 않습니다"),

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE-001", "수업을 찾을 수 없습니다"),
    JOIN_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE-002", "참여 코드에 해당하는 수업이 없습니다"),
    ALREADY_ENROLLED(HttpStatus.CONFLICT, "COURSE-003", "이미 참여한 수업입니다"),
    COURSE_FORBIDDEN(HttpStatus.FORBIDDEN, "COURSE-004", "해당 수업에 대한 권한이 없습니다"),

    ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ASSIGNMENT-001", "과제를 찾을 수 없습니다"),
    PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "PROBLEM-001", "문제를 찾을 수 없습니다"),

    SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBMISSION-001", "제출을 찾을 수 없습니다"),
    DEADLINE_PASSED(HttpStatus.CONFLICT, "SUBMISSION-002", "마감이 지났습니다"),
    RESUBMIT_NOT_ALLOWED(HttpStatus.CONFLICT, "SUBMISSION-003", "재제출할 수 없는 문제입니다"),
    CODE_TOO_LARGE(HttpStatus.BAD_REQUEST, "SUBMISSION-004", "코드가 너무 큽니다"),
    SUBMISSION_FORBIDDEN(HttpStatus.FORBIDDEN, "SUBMISSION-005", "해당 제출에 대한 권한이 없습니다");

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
