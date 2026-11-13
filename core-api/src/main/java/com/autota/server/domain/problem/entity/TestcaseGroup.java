package com.autota.server.domain.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Must에서는 문제 등록 때 서버가 그룹 1개(100점, RATIO)를 만들어 모든 케이스를 넣는다. S2에서 교수가 나눈다. */
@Entity
@Table(name = "testcase_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestcaseGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    private String name;

    /** 그룹 배점. 그룹 배점의 합이 문제 배점이다. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ScoringMode scoringMode;

    @Column(nullable = false)
    private int orderNo;

    public TestcaseGroup(Problem problem, String name, BigDecimal score, ScoringMode scoringMode, int orderNo) {
        this.problem = problem;
        this.name = name;
        this.score = score;
        this.scoringMode = scoringMode;
        this.orderNo = orderNo;
    }
}
