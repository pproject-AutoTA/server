package com.autota.server.domain.submission.repository;

import com.autota.server.domain.submission.entity.SubmissionStatus;
import com.autota.server.domain.submission.entity.Verdict;

import java.time.Instant;

/** 제출 이력 한 줄. code(최대 64KB)를 읽지 않으려고 필요한 컬럼만 조회한다. */
public record SubmissionSummary(
        Long id,
        SubmissionStatus status,
        Verdict verdict,       // 채점 전이면 null
        Integer passedCount,   // 채점 전이면 null
        Instant submittedAt
) {
}
