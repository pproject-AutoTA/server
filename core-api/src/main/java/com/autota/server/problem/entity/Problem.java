package com.autota.server.problem.entity;

import com.autota.server.assignment.entity.Assignment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 문제 (M3). 테스트케이스·그룹·검사 규칙을 함께 들고 다닌다(같이 저장·삭제).
 */
@Entity
@Table(name = "problems")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(name = "order_no", nullable = false)
    private int orderNo = 1;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "input_format", nullable = false)
    private String inputFormat;

    @Column(name = "output_format", nullable = false)
    private String outputFormat;

    /** CPU 시간 기준 (100 ~ 60000) */
    @Column(name = "cpu_time_limit_ms", nullable = false)
    private int timeLimitMs = 2000;

    @Column(name = "memory_limit_mb", nullable = false)
    private int memoryLimitMb = 256;

    @Column(name = "allow_resubmit", nullable = false)
    private boolean allowResubmit = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemStatus status = ProblemStatus.PUBLISHED;

    @Enumerated(EnumType.STRING)
    @Column(name = "lint_mode", nullable = false)
    private LintMode lintMode = LintMode.DENYLIST;

    /** 모범답안 (S4) */
    @Column(name = "reference_code")
    private String referenceCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNo")
    private List<TestcaseGroup> groups = new ArrayList<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNo")
    private List<Testcase> testcases = new ArrayList<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemLintRule> lintRules = new ArrayList<>();

    public Problem(Assignment assignment, int orderNo, String title, String description,
                   String inputFormat, String outputFormat, int timeLimitMs, int memoryLimitMb, boolean allowResubmit) {
        this.assignment = assignment;
        this.orderNo = orderNo;
        this.title = title;
        this.description = description;
        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
        this.timeLimitMs = timeLimitMs;
        this.memoryLimitMb = memoryLimitMb;
        this.allowResubmit = allowResubmit;
    }

    public TestcaseGroup addGroup(String name, java.math.BigDecimal points, ScoringMode mode) {
        TestcaseGroup group = new TestcaseGroup(this, groups.size() + 1, name, points, mode);
        groups.add(group);
        return group;
    }

    public Testcase addTestcase(TestcaseGroup group, String input, String expectedOutput, boolean isPublic) {
        Testcase tc = new Testcase(this, group, testcases.size() + 1, input, expectedOutput, isPublic);
        testcases.add(tc);
        return tc;
    }

    public void addLintRule(LintRuleKind kind, String tag) {
        lintRules.add(new ProblemLintRule(this, kind, tag));
    }

    public void changeLintMode(LintMode mode) {
        this.lintMode = mode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }
}
