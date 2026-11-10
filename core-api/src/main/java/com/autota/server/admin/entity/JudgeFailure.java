package com.autota.server.admin.entity;

import com.autota.server.submission.entity.Submission;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/** 채점 최종 실패 이력 (ADMIN-02). 최종 실패한 제출당 한 행. 학생 코드의 RE는 넣지 않는다. */
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

    /** 재시도 횟수 (API retryCount) */
    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    /** 워커가 남긴 메시지 */
    @Column(nullable = false)
    private String reason;

    @Column(name = "failed_at", nullable = false)
    private Instant failedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public JudgeFailure(Submission submission, int retryCount, String reason, Instant failedAt) {
        this.submission = submission;
        this.retryCount = retryCount;
        this.reason = reason;
        this.failedAt = failedAt;
    }
}
