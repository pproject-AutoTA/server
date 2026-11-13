package com.autota.server.domain.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "problems")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** assignments.id. 다른 도메인이라 연관관계 대신 id만 둔다. */
    @Column(nullable = false)
    private Long assignmentId;

    /** 1~100자. 길이·범위 검증은 API(400)에서. */
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String inputFormat;

    @Column(nullable = false)
    private String outputFormat;

    /** CPU 시간 기준(ADR 0004). API에서는 timeLimitMs. 100~10000. */
    @Column(nullable = false)
    private int cpuTimeLimitMs;

    /** 32~512. */
    @Column(nullable = false)
    private int memoryLimitMb;

    /** API에서는 resubmitAllowed. false면 두 번째 제출은 409(SUBMISSION-003). */
    @Column(nullable = false)
    private boolean allowResubmit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ProblemStatus status;

    /** 과제 안 표시 순서. */
    @Column(nullable = false)
    private int orderNo;

    public Problem(Long assignmentId, String title, String description, String inputFormat, String outputFormat,
                   int cpuTimeLimitMs, int memoryLimitMb, boolean allowResubmit, int orderNo) {
        this.assignmentId = assignmentId;
        this.title = title;
        this.description = description;
        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
        this.cpuTimeLimitMs = cpuTimeLimitMs;
        this.memoryLimitMb = memoryLimitMb;
        this.allowResubmit = allowResubmit;
        this.orderNo = orderNo;
        this.status = ProblemStatus.PUBLISHED; // Must에서는 등록 즉시 학생에게 보인다
    }
}
