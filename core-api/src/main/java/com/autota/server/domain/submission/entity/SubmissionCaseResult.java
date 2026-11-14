package com.autota.server.domain.submission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 케이스별 채점 결과. 비공개 케이스 행도 저장한다. */
@Entity
@Table(name = "submission_case_results",
        uniqueConstraints = @UniqueConstraint(columnNames = {"submission_id", "testcase_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionCaseResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    /** 다른 도메인(problem)이라 id만 들고 있는다. */
    @Column(nullable = false)
    private Long testcaseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Verdict verdict;

    /** API의 timeMs. SKIPPED면 null. */
    private Integer cpuTimeMs;

    /** SKIPPED면 null. */
    private Integer memoryKb;

    /** 학생의 실제 출력. 1KB에서 잘라 저장한다. */
    @Column(columnDefinition = "text")
    private String actualOutput;

    public SubmissionCaseResult(Submission submission, Long testcaseId, Verdict verdict,
                                Integer cpuTimeMs, Integer memoryKb, String actualOutput) {
        this.submission = submission;
        this.testcaseId = testcaseId;
        this.verdict = verdict;
        this.cpuTimeMs = cpuTimeMs;
        this.memoryKb = memoryKb;
        this.actualOutput = actualOutput;
    }
}
