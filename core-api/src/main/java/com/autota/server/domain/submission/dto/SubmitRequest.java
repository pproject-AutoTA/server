package com.autota.server.domain.submission.dto;

import jakarta.validation.constraints.NotBlank;

/** 64KB 상한은 바이트 기준이고 에러 코드가 달라(SUBMISSION-004) 서비스에서 검사한다. */
public record SubmitRequest(
        @NotBlank String code
) {
}
