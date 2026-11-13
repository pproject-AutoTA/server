package com.autota.server.domain.problem.dto;

import java.time.LocalDateTime;
import java.util.List;

/** 문제 상세(학생). 테스트케이스는 공개 케이스만 내리고, 비공개는 개수에만 포함한다. */
public record StudentProblemResponse (
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
        /** 공개 케이스만 */
        List<Testcase> testcases,
        /** 공개+비공개 합계 */
        int totalTestcaseCount
){
    public record Testcase(
            /** 엔티티의 orderNo */
            int no,
            String input,
            String expectedOutput
    ) {
    }
}
