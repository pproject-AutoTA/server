package com.autota.server.admin.entity;

import com.autota.server.submission.entity.Submission;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/** 채점 실패 이력 (M7 관리자 화면) */
@Entity
@Table(name = "job_failures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkerType worker;

    @Column(nullable = false)
    private int attempt = 1;

    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    /** 반복 실패로 DLQ에 격리됐는지 */
    @Column(name = "dead_lettered", nullable = false)
    private boolean deadLettered;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public JobFailure(Submission submission, WorkerType worker, int attempt,
                      String errorMessage, boolean deadLettered) {
        this.submission = submission;
        this.worker = worker;
        this.attempt = attempt;
        this.errorMessage = errorMessage;
        this.deadLettered = deadLettered;
    }
}
