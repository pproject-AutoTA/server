package com.autota.server.submission.entity;

import com.autota.server.problem.entity.Testcase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 케이스별 결과 (M6). 비공개 케이스는 마감 전엔 개수만 보여준다. */
@Entity
@Table(name = "submission_case_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionCaseResult {

    @EmbeddedId
    private SubmissionCaseResultId id;

    @MapsId("submissionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @MapsId("testcaseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "testcase_id")
    private Testcase testcase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Verdict verdict;

    @Column(name = "cpu_time_ms")
    private Integer cpuTimeMs;

    @Column(name = "memory_kb")
    private Integer memoryKb;

    @Column(name = "stdout_head")
    private String stdoutHead;   // 화면용 앞부분만

    @Column(name = "stderr_head")
    private String stderrHead;

    SubmissionCaseResult(Submission submission, Testcase testcase, Verdict verdict,
                         Integer cpuTimeMs, Integer memoryKb, String stdoutHead, String stderrHead) {
        this.id = new SubmissionCaseResultId(submission.getId(), testcase.getId());
        this.submission = submission;
        this.testcase = testcase;
        this.verdict = verdict;
        this.cpuTimeMs = cpuTimeMs;
        this.memoryKb = memoryKb;
        this.stdoutHead = stdoutHead;
        this.stderrHead = stderrHead;
    }

    public boolean isPassed() {
        return verdict == Verdict.AC;
    }
}
