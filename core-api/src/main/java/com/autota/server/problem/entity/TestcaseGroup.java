package com.autota.server.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 테스트케이스 그룹 (S2). 그룹마다 배점과 채점 방식을 가진다. */
@Entity
@Table(name = "testcase_groups",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id", "problem_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestcaseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "order_no", nullable = false)
    private int orderNo;

    @Column
    private String name;

    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal points;

    @Enumerated(EnumType.STRING)
    @Column(name = "scoring_mode", nullable = false)
    private ScoringMode scoringMode = ScoringMode.RATIO;

    TestcaseGroup(Problem problem, int orderNo, String name, BigDecimal points, ScoringMode mode) {
        this.problem = problem;
        this.orderNo = orderNo;
        this.name = name;
        this.points = points;
        this.scoringMode = mode;
    }

    /** 그룹 점수 = 배점 × 통과 비율 (전체 통과 방식이면 전부 맞아야 배점) */
    public BigDecimal score(int passed, int total) {
        if (total == 0) return BigDecimal.ZERO;
        if (scoringMode == ScoringMode.ALL_OR_NOTHING) {
            return passed == total ? points : BigDecimal.ZERO;
        }
        return points.multiply(BigDecimal.valueOf(passed))
                     .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}
