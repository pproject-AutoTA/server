package com.autota.server.global.common;

/**
 * 성공 응답. 필드는 항상 4개이고 data가 없으면 null이다.
 * (null 필드를 생략하지 않는다. @JsonInclude(NON_NULL) 금지)
 */
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "COMMON-200", "요청에 성공했습니다.", data);
    }

    /** 반환할 데이터가 없는 성공 (data: null) */
    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, "COMMON-201", "요청에 성공했습니다.", data);
    }

    public static <T> ApiResponse<T> accepted(T data) {
        return new ApiResponse<>(true, "COMMON-202", "요청에 성공했습니다.", data);
    }
}
