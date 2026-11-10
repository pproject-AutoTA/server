package com.autota.server.submission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** FLAG 근거 (S1). 태그 + 줄 번호. 교수에게만 보인다. */
@Entity
@Table(name = "lint_findings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LintFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(nullable = false)
    private String tag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LintReason reason;

    @Column(name = "line_no")
    private Integer lineNo;   // MISSING_REQUIRED는 null

    @Column(name = "col_no")
    private Integer colNo;

    LintFinding(Submission submission, String tag, LintReason reason, Integer lineNo, Integer colNo) {
        this.submission = submission;
        this.tag = tag;
        this.reason = reason;
        this.lineNo = lineNo;
        this.colNo = colNo;
    }
}
