package com.autota.server.problem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProblemLintRuleId implements Serializable {

    @Column(name = "problem_id")
    private Long problemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind")
    private LintRuleKind kind;

    @Column(name = "tag")
    private String tag;
}
