package com.autota.server.global.common;

import com.autota.server.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/** data는 항상 포함(없으면 null), 나머지 null 필드는 생략한다. */
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        @JsonInclude(JsonInclude.Include.NON_NULL) String timestamp,
        @JsonInclude(JsonInclude.Include.NON_NULL) String path,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<FieldError> errors
) {
    public record FieldError(String field, Object rejectedValue, String reason) {}

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "COMMON-200", "요청에 성공했습니다.", data, null, null, null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, "COMMON-201", "생성되었습니다.", data, null, null, null);
    }

    public static <T> ApiResponse<T> accepted(T data) {
        return new ApiResponse<>(true, "COMMON-202", "요청이 접수되었습니다.", data, null, null, null);
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode, String path) {
        return fail(errorCode, path, null);
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode, String path, List<FieldError> errors) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null,
                LocalDateTime.now().toString(), path, errors);
    }
}
