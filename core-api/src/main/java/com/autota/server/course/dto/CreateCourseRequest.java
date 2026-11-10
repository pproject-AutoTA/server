package com.autota.server.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCourseRequest(
        @NotBlank(message = "수업명은 필수입니다")
        @Size(min = 1, max = 50, message = "수업명은 1~50자여야 합니다")
        String name,

        @NotBlank(message = "학기는 필수입니다")
        @Pattern(regexp = "^\\d{4}-\\d$", message = "학기는 YYYY-N 형식이어야 합니다")
        String semester
) {
}
