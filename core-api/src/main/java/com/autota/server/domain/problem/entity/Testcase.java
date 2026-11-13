package com.autota.server.domain.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "testcases",
        uniqueConstraints = @UniqueConstraint(columnNames = {"problem_id", "order_no"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Testcase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 그룹을 거치지 않고 바로 조회하려고 문제도 직접 가리킨다. group의 문제와 같아야 한다(DB 복합 FK). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private TestcaseGroup group;

    @Column(nullable = false)
    private String input;

    @Column(nullable = false)
    private String expectedOutput;

    /** 비공개 케이스는 학생에게 통과 개수만 보여 준다. */
    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    /** 문제 안 순서. 응답의 testcases[].no. */
    @Column(nullable = false)
    private int orderNo;

    public Testcase(Problem problem, TestcaseGroup group, String input, String expectedOutput,
                    boolean isPublic, int orderNo) {
        this.problem = problem;
        this.group = group;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.isPublic = isPublic;
        this.orderNo = orderNo;
    }
}
