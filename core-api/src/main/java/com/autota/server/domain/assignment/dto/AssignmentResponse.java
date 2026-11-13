package com.autota.server.domain.assignment.dto;

import java.time.LocalDateTime;
import java.util.List;

/** 과제·문제 목록의 과제 1건. 역할에 따라 값이 없는 필드는 빼지 않고 null로 내린다. */
public record AssignmentResponse(
        Long id,
        String title,
        LocalDateTime dueAt,
        /** 교수만 값, 학생은 null */
        Integer studentCount,
        /** TODO(problem): problem 도메인이 생길 때까지 항상 빈 배열이다. */
        List<Problem> problems
) {
    /** TODO(problem·submission): 아직 값을 채우는 코드가 없다. 응답 모양만 명세(3번)대로 잡아 둔 것이다. */
    public record Problem(
            Long id,
            String title,
            boolean resubmitAllowed,
            /** 공개+비공개 합계 */
            int testcaseCount,
            /** 교수만 값, 학생은 null */
            Integer submittedStudentCount,
            /** 학생만 값. 제출이 없거나 교수면 null */
            MySubmission mySubmission
    ) {
    }

    /** TODO(submission): status·verdict는 submission 도메인에 enum이 생기면 String 대신 그 타입으로 바꾼다. */
    public record MySubmission(
            Long submissionId,
            /** PENDING, RUNNING, DONE, FAILED */
            String status,
            /** 채점 전에는 null */
            String verdict,
            /** 채점 전에는 null */
            Integer passedCount,
            int totalCount,
            LocalDateTime submittedAt
    ) {
    }
}
