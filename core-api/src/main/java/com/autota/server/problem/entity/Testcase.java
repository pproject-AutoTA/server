package com.autota.server.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 테스트케이스 (M3). 공개/비공개. 그룹은 S2부터 (없어도 된다). */
@Entity
@Table(name = "testcases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Testcase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private TestcaseGroup group;

    @Column(name = "order_no", nullable = false)
    private int orderNo;

    @Column(nullable = false)
    private String input;

    @Column(name = "expected_output", nullable = false)
    private String expectedOutput;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    Testcase(Problem problem, TestcaseGroup group, int orderNo,
             String input, String expectedOutput, boolean isPublic) {
        this.problem = problem;
        this.group = group;
        this.orderNo = orderNo;
        this.input = input == null ? "" : input;
        this.expectedOutput = expectedOutput;
        this.isPublic = isPublic;
    }
}
