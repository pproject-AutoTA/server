package com.autota.server.course.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinCourseRequest(
        @NotBlank(message = "참여 코드는 필수입니다") String joinCode
) {
}
