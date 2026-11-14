package com.autota.server.domain.submission.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/** 내 제출 상세. 값이 없는 필드도 빼지 않고 null로 내린다. */
public record MySubmissionDetailResponse(
        Long id,
        Long problemId,
        String status,
        String verdict,            // DONE이 아니면 null
        String code,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime submittedAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime judgedAt,    // 채점 전이면 null
        Integer executionTimeMs,   // 케이스 중 최댓값, 채점 전이면 null
        Integer memoryKb,          // 케이스 중 최댓값, 채점 전이면 null
        List<Testcase> testcases,  // 채점 전·FAILED면 빈 배열. 마감 전에는 공개 케이스만
        Integer hiddenPassed,      // 마감 후에는 null
        Integer hiddenTotal,       // 마감 후에는 null
        boolean flagged            // Must에서는 항상 false
) {
    public record Testcase(
            int no,
            @JsonProperty("isPublic")
            boolean isPublic,
            String verdict,
            Integer timeMs,        // SKIPPED면 null
            String input,
            String expectedOutput,
            String actualOutput    // 1KB까지
    ) {
    }
}
