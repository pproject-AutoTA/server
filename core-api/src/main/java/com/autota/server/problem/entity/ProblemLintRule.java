package com.autota.server.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * 허용 요소 검사 규칙 (S1, 7절).
 * 태그 형식: import:모듈 / call:이름 / syntax:노드  예) call:sorted
 */
@Entity
@Table(name = "problem_lint_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemLintRule {

    private static final Pattern TAG = Pattern.compile("^(import|call|syntax):[A-Za-z_][A-Za-z0-9_.]*$");

    @EmbeddedId
    private ProblemLintRuleId id;

    @MapsId("problemId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    ProblemLintRule(Problem problem, LintRuleKind kind, String tag) {
        if (!TAG.matcher(tag).matches()) {
            throw new IllegalArgumentException("잘못된 태그: " + tag);
        }
        this.id = new ProblemLintRuleId(problem.getId(), kind, tag);
        this.problem = problem;
    }

    public LintRuleKind getKind() {
        return id.getKind();
    }

    public String getTag() {
        return id.getTag();
    }
}
