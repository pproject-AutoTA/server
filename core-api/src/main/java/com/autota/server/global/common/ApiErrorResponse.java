package com.autota.server.global.common;

import com.autota.server.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 실패 응답. 필드는 항상 7개이고 data는 항상 null, errors는 검증 실패일 때만 값이 있고 아니면 null이다.
 * (null 필드를 생략하지 않는다. @JsonInclude(NON_NULL) 금지)
 */
public record ApiErrorResponse(
        boolean success,
        String code,
        String message,
        Void data,
        String timestamp,
        String path,
        List<FieldError> errors
) {
    /** 서버 기본 시간대와 무관하게 같은 값이 나오도록 고정한다. */
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    /** 명세 예시 형식(2026-11-07T12:34:56.789). 초·밀리초가 0이어도 자릿수를 유지한다. */
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public record FieldError(String field, Object rejectedValue, String reason) {}

    private static String now() {
        return LocalDateTime.now(ZONE).format(FORMAT);
    }

    /** ErrorCode에 없는 상태(404, 405 등)를 그대로 내려줄 때 */
    public static ApiErrorResponse of(String code, String message, String path) {
        return new ApiErrorResponse(false, code, message, null, now(), path, null);
    }

    public static ApiErrorResponse of(ErrorCode errorCode, String path) {
        return of(errorCode, path, null);
    }

    public static ApiErrorResponse of(ErrorCode errorCode, String path, List<FieldError> errors) {
        return new ApiErrorResponse(false, errorCode.getCode(), errorCode.getMessage(), null, now(), path, errors);
    }
}
