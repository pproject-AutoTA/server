package com.autota.server.domain.assignment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDateTime;

public record AssignmentRequest(
        @NotBlank(message = "과제명은 필수입니다")
        @Size(max = 100, message = "과제명은 100자 이하여야 합니다")
        String title,

        @NotNull(message = "마감 시각은 필수입니다")
        @Future(message = "마감 시각은 현재 이후여야 합니다")
        Instant dueAt
) {
}
