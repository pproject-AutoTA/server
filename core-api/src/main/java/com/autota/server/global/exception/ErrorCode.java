package com.autota.server.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    COMMON_400(HttpStatus.BAD_REQUEST, "COMMON-400", "입력값이 올바르지 않습니다"),
    COMMON_401(HttpStatus.UNAUTHORIZED, "COMMON-401", "인증이 필요합니다"),
    COMMON_403(HttpStatus.FORBIDDEN, "COMMON-403", "권한이 없습니다"),
    COMMON_404(HttpStatus.NOT_FOUND, "COMMON-404", "요청한 경로를 찾을 수 없습니다"),
    COMMON_405(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-405", "지원하지 않는 요청 방식입니다"),
    COMMON_500(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 오류입니다"),

    AUTH_001(HttpStatus.UNAUTHORIZED, "AUTH-001", "유효하지 않은 토큰입니다"),
    AUTH_002(HttpStatus.UNAUTHORIZED, "AUTH-002", "만료된 토큰입니다"),
    AUTH_003(HttpStatus.UNAUTHORIZED, "AUTH-003", "이메일 또는 비밀번호가 올바르지 않습니다"),

    COURSE_001(HttpStatus.NOT_FOUND, "COURSE-001", "수업을 찾을 수 없습니다"),
    COURSE_002(HttpStatus.NOT_FOUND, "COURSE-002", "참여 코드에 해당하는 수업이 없습니다"),
    COURSE_003(HttpStatus.CONFLICT, "COURSE-003", "이미 참여한 수업입니다"),
    COURSE_004(HttpStatus.FORBIDDEN, "COURSE-004", "해당 수업에 대한 권한이 없습니다"),

    ASSIGNMENT_001(HttpStatus.NOT_FOUND, "ASSIGNMENT-001", "과제를 찾을 수 없습니다"),
    PROBLEM_001(HttpStatus.NOT_FOUND, "PROBLEM-001", "문제를 찾을 수 없습니다"),

    SUBMISSION_001(HttpStatus.NOT_FOUND, "SUBMISSION-001", "제출을 찾을 수 없습니다"),
    SUBMISSION_002(HttpStatus.CONFLICT, "SUBMISSION-002", "마감이 지났습니다"),
    SUBMISSION_003(HttpStatus.CONFLICT, "SUBMISSION-003", "재제출할 수 없는 문제입니다"),
    SUBMISSION_004(HttpStatus.BAD_REQUEST, "SUBMISSION-004", "코드가 너무 큽니다"),
    SUBMISSION_005(HttpStatus.FORBIDDEN, "SUBMISSION-005", "해당 제출에 대한 권한이 없습니다");

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
