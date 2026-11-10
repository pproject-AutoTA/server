package com.autota.server.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 모든 API가 공유하는 응답 틀. 도메인마다 data 안의 내용만 다르다.
 * data는 항상 내려가고(없으면 null), 나머지 null 필드(timestamp, path, errors)는 생략된다.
 * 응답 예시는 core-api/src/apiResponse.md 참고.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.ALWAYS) T data,
        LocalDateTime timestamp,   // 실패 응답에만
        String path,               // 실패 응답에만
        List<FieldError> errors    // 입력값 검증 실패에만
) {
    private static final String SUCCESS_MESSAGE = "요청에 성공했습니다.";

    /** 필드 단위 검증 에러 */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record FieldError(String field, Object rejectedValue, String reason) {
    }

    /** 조회·수정 성공 (COMMON-200) */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "COMMON-200", SUCCESS_MESSAGE, data, null, null, null);
    }

    /** 반환할 데이터가 없는 성공 (COMMON-200, data: null) */
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, "COMMON-200", SUCCESS_MESSAGE, null, null, null, null);
    }

    /** 생성 성공 (COMMON-201) */
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, "COMMON-201", SUCCESS_MESSAGE, data, null, null, null);
    }

    /** 실패 (data: null) */
    public static <T> ApiResponse<T> fail(String code, String message, String path) {
        return new ApiResponse<>(false, code, message, null, LocalDateTime.now(), path, null);
    }

    /** 입력값 검증 실패 (errors 포함) */
    public static <T> ApiResponse<T> fail(String code, String message, String path, List<FieldError> errors) {
        return new ApiResponse<>(false, code, message, null, LocalDateTime.now(), path, errors);
    }
}
