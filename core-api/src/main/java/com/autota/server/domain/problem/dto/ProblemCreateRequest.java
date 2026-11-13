package com.autota.server.domain.problem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 문제 등록 요청. 테스트케이스와 함께 한 번에 등록한다. */
public record ProblemCreateRequest(
        @NotBlank(message = "문제 제목은 필수입니다")
        @Size(max = 100, message = "문제 제목은 100자 이하여야 합니다")
        String title,

        @NotBlank(message = "문제 설명은 필수입니다")
        String description,

        @NotBlank(message = "입력 형식은 필수입니다")
        String inputFormat,

        @NotBlank(message = "출력 형식은 필수입니다")
        String outputFormat,

        @NotNull(message = "시간 제한은 필수입니다")
        @Min(value = 100, message = "시간 제한은 100ms 이상이어야 합니다")
        @Max(value = 10000, message = "시간 제한은 10000ms 이하여야 합니다")
        Integer timeLimitMs,

        @NotNull(message = "메모리 제한은 필수입니다")
        @Min(value = 32, message = "메모리 제한은 32MB 이상이어야 합니다")
        @Max(value = 512, message = "메모리 제한은 512MB 이하여야 합니다")
        Integer memoryLimitMb,

        @NotNull(message = "재제출 허용 여부는 필수입니다")
        Boolean resubmitAllowed,

        @NotEmpty(message = "테스트케이스는 1건 이상이어야 합니다")
        @Valid
        List<Testcase> testcases
) {
    public record Testcase(
            /** 입력이 없는 문제도 있어 빈 문자열은 허용한다. */
            @NotNull(message = "입력은 필수입니다")
            String input,

            @NotNull(message = "기대 출력은 필수입니다")
            String expectedOutput,

            @JsonProperty("isPublic") // 이름을 고정하지 않으면 JSON에서 "public"으로 읽힐 수 있다
            @NotNull(message = "공개 여부는 필수입니다")
            Boolean isPublic
    ) {
    }
}
