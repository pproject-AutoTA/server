package com.autota.server.submission.entity;

import com.autota.server.problem.entity.Problem;
import com.autota.server.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 제출 (M4~M6).
 * 저장할 때 DB 트리거가 한 번 더 막는다:
 *   NOT_ENROLLED / DEADLINE_PASSED / RESUBMIT_NOT_ALLOWED
 */
@Entity
@Table(name = "submissions",
        indexes = @Index(name = "idx_submissions_problem_student_submitted",
                columnList = "problem_id, student_id, submitted_at DESC"),
        check = @CheckConstraint(constraint = "octet_length(code) <= 65536"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission {

    public static final int MAX_CODE_BYTES = 64 * 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private Verdict verdict;

    @Column(name = "passed_count")
    private Integer passedCount;

    @Column(name = "hidden_passed_count")
    private Integer hiddenPassedCount;

    /** 케이스 중 최댓값 (API executionTimeMs) */
    @Column(name = "cpu_time_ms")
    private Integer cpuTimeMs;

    /** 케이스 중 최댓값 */
    @Column(name = "memory_kb")
    private Integer memoryKb;

    @Enumerated(EnumType.STRING)
    @Column(name = "lint_status", nullable = false)
    private LintStatus lintStatus = LintStatus.PENDING;

    /** 트리거가 마감 비교에 쓰므로 Java에서 직접 채운다 */
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @Column(name = "judged_at")
    private Instant judgedAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmissionCaseResult> caseResults = new ArrayList<>();

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LintFinding> lintFindings = new ArrayList<>();

    public Submission(Problem problem, User student, String code, Instant now) {
        if (code.getBytes(StandardCharsets.UTF_8).length > MAX_CODE_BYTES) {
            throw new IllegalArgumentException("코드는 64KB까지 제출할 수 있습니다");
        }
        this.problem = problem;
        this.student = student;
        this.code = code;
        this.submittedAt = now;
    }

    // ---- 채점 결과 반영 (judge-worker가 보낸 JudgeResult를 받아서) ----

    public void startJudging() {
        this.status = SubmissionStatus.RUNNING;
    }

    public SubmissionCaseResult addCaseResult(com.autota.server.problem.entity.Testcase testcase, Verdict verdict,
                                              Integer cpuTimeMs, Integer memoryKb,
                                              String actualOutput) {
        SubmissionCaseResult r = new SubmissionCaseResult(this, testcase, verdict, cpuTimeMs, memoryKb, actualOutput);
        caseResults.add(r);
        return r;
    }

    /** 전체 판정: 모두 AC면 AC, 아니면 처음 실패한 케이스의 판정 (6절) */
    public void complete(Verdict overall, int passed, int hiddenPassed,
                         Integer cpuTimeMs, Integer memoryKb, Instant now) {
        this.status = SubmissionStatus.DONE;
        this.verdict = overall;
        this.passedCount = passed;
        this.hiddenPassedCount = hiddenPassed;
        this.cpuTimeMs = cpuTimeMs;
        this.memoryKb = memoryKb;
        this.judgedAt = now;
    }

    /** 시스템 오류로 채점 못 함 (학생 코드 잘못이 아님) */
    public void fail(Instant now) {
        this.status = SubmissionStatus.FAILED;
        this.judgedAt = now;
    }

    // ---- 허용 요소 검사 결과 반영 (lint-worker) ----

    public void addLintFinding(String tag, LintReason reason, Integer line, Integer col) {
        lintFindings.add(new LintFinding(this, tag, reason, line, col));
        this.lintStatus = LintStatus.FLAGGED;
    }

    public void markLintClean() {
        if (lintFindings.isEmpty()) {
            this.lintStatus = LintStatus.CLEAN;
        }
    }

    public void markLintError() {
        this.lintStatus = LintStatus.ERROR;
    }

    public boolean isFlagged() {
        return lintStatus == LintStatus.FLAGGED;
    }
}
