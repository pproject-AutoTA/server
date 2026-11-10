package com.autota.server.submission.entity;

import com.autota.server.problem.entity.Testcase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

/** 케이스별 결과 (M6). 비공개 케이스는 마감 전엔 개수만 보여준다. */
@Entity
@Table(name = "submission_case_results",
        uniqueConstraints = @UniqueConstraint(columnNames = {"submission_id", "testcase_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionCaseResult {

    public static final int MAX_OUTPUT_BYTES = 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "testcase_id", nullable = false)
    private Testcase testcase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Verdict verdict;

    @Column(name = "cpu_time_ms")
    private Integer cpuTimeMs;

    @Column(name = "memory_kb")
    private Integer memoryKb;

    /** 학생의 실제 출력. 1KB에서 자른다. */
    @Column(name = "actual_output")
    private String actualOutput;

    SubmissionCaseResult(Submission submission, Testcase testcase, Verdict verdict,
                         Integer cpuTimeMs, Integer memoryKb, String actualOutput) {
        this.submission = submission;
        this.testcase = testcase;
        this.verdict = verdict;
        this.cpuTimeMs = cpuTimeMs;
        this.memoryKb = memoryKb;
        this.actualOutput = truncate(actualOutput);
    }

    /** UTF-8 기준 1KB를 넘으면 문자 경계에서 자른다. */
    private static String truncate(String s) {
        if (s == null) return null;
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= MAX_OUTPUT_BYTES) return s;
        int end = MAX_OUTPUT_BYTES;
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) end--; // 문자 중간이면 앞으로
        return new String(bytes, 0, end, StandardCharsets.UTF_8);
    }

    public boolean isPassed() {
        return verdict == Verdict.AC;
    }
}
