package com.autota.server.domain.course.dto;

import jakarta.validation.constraints.NotBlank;

public record StudentCourseRequest (
        @NotBlank(message = "참여 코드는 필수입니다")
        String joinCode
) {
}
