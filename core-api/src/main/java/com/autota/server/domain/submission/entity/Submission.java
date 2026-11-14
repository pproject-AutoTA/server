package com.autota.server.domain.submission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "submissions",
        indexes = @Index(columnList = "problem_id, student_id, submitted_at DESC"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission {
    /** 큐 메시지의 submission_id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 다른 도메인(problem)이라 id만 들고 있는다. */
    @Column(nullable = false)
    private Long problemId;

    /** 다른 도메인(user)이라 id만 들고 있는다. */
    @Column(nullable = false)
    private Long studentId;

    /** 64KB 상한은 API 검증과 DB CHECK가 막는다(SUBMISSION-004). */
    @Column(nullable = false, columnDefinition = "text")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SubmissionStatus status;

    /** DONE일 때만 값이 있다. FLAG는 판정이 아니다(lint_results.flagged). */
    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private Verdict verdict;

    /** 케이스 중 최댓값. API의 executionTimeMs. */
    private Integer cpuTimeMs;

    /** 케이스 중 최댓값. */
    private Integer memoryKb;

    private Integer passedCount;

    /** 비공개 케이스 통과 수. API의 hiddenPassed. */
    private Integer hiddenPassedCount;

    /** 서버 수신 시각. 마감 판정 기준이다. */
    @Column(nullable = false)
    private Instant submittedAt;

    /** 결과 반영 시각. DONE·FAILED에서 채운다. */
    private Instant judgedAt;

    public Submission(Long problemId, Long studentId, String code) {
        this.problemId = problemId;
        this.studentId = studentId;
        this.code = code;
        this.status = SubmissionStatus.PENDING;
        this.submittedAt = Instant.now();
    }
}
