package com.autota.server.problem;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
){
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(
                true,
                "COMMON-200",
                "요청에 성공했습니다.",
                data
        );
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
