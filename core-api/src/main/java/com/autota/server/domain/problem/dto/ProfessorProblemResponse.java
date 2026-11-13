package com.autota.server.domain.problem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/** 문제 상세(교수). 비공개 케이스까지 전부 내린다. */
public record ProfessorProblemResponse(
        Long id,
        Long assignmentId,
        Long courseId,
        String title,
        String description,
        String inputFormat,
        String outputFormat,
        /** 엔티티의 cpuTimeLimitMs */
        int timeLimitMs,
        int memoryLimitMb,
        /** 엔티티의 allowResubmit */
        boolean resubmitAllowed,
        /** 과제 마감. Asia/Seoul 기준 */
        LocalDateTime dueAt,
        List<Testcase> testcases
) {
    public record Testcase(
            /** 엔티티의 orderNo */
            int no,
            String input,
            String expectedOutput,
            @JsonProperty("isPublic") // 이름을 고정하지 않으면 JSON에서 "public"으로 나갈 수 있다
            boolean isPublic
    ) {
    }
}
