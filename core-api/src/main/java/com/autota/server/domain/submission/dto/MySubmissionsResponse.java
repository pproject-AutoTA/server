package com.autota.server.domain.submission.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record MySubmissionsResponse(
        long id,
        String status,
        String verdict,
        Integer passedCount, // 채점 전이면 null
        int totalCount,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime submittedAt,
        @JsonProperty("isLatest")
        boolean isLatest
) {
}
