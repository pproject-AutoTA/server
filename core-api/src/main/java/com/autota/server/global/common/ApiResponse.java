package com.autota.server.global.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

/**
 * 성공 응답. 필드는 항상 4개이고 data가 없으면 null이다.
 * (null 필드를 생략하지 않는다. @JsonInclude(NON_NULL) 금지)
 * HTTP 상태는 ApiResponseStatusAdvice가 httpStatus()로 맞추므로 컨트롤러에 @ResponseStatus를 붙이지 않는다.
 */
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok(T data) {
        return of(HttpStatus.OK, data);
    }

    /** 반환할 데이터가 없는 성공 (data: null) */
    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return of(HttpStatus.CREATED, data);
    }

    public static <T> ApiResponse<T> accepted(T data) {
        return of(HttpStatus.ACCEPTED, data);
    }

    /** 성공 코드는 COMMON-{HTTP 상태}다(COMMON-200·201·202). */
    private static <T> ApiResponse<T> of(HttpStatus status, T data) {
        return new ApiResponse<>(true, "COMMON-" + status.value(), "요청에 성공했습니다.", data);
    }

    /** code(COMMON-201)의 숫자가 곧 HTTP 상태다. 응답 본문에는 나가지 않는다. */
    @JsonIgnore
    public HttpStatus httpStatus() {
        return HttpStatus.valueOf(Integer.parseInt(code.substring(code.indexOf('-') + 1)));
    }
}
