package com.autota.server.domain.submission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** 3회 재시도 후 최종 실패한 제출당 한 행. 학생 코드의 RE는 여기 들어오지 않는다. */
@Entity
@Table(name = "judge_failures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JudgeFailure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private Submission submission;

    @Column(nullable = false)
    private int retryCount;

    /** JudgeResult.error의 "code: message". dead letter 경로면 delivery_limit. */
    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Column(nullable = false)
    private Instant failedAt;

    public JudgeFailure(Submission submission, int retryCount, String reason, Instant failedAt) {
        this.submission = submission;
        this.retryCount = retryCount;
        this.reason = reason;
        this.failedAt = failedAt;
    }
}
