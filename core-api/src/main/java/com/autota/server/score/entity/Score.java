package com.autota.server.score.entity;

import com.autota.server.problem.entity.Problem;
import com.autota.server.submission.entity.Submission;
import com.autota.server.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 점수 (S2, S3). 학생 × 문제마다 1줄.
 * 확정 점수 = 자동 점수 − 지각 감점 ± 교수 조정  (DB가 계산, 소수 첫째 자리까지)
 * 공개된 과제의 점수를 바꾸면 DB 트리거가 score_history에 기록한다.
 */
@Entity
@Table(name = "scores",
       uniqueConstraints = @UniqueConstraint(columnNames = {"problem_id", "student_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /** 성적 대상 제출 = 마지막 제출 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @Column(name = "auto_score", nullable = false, precision = 7, scale = 2)
    private BigDecimal autoScore = BigDecimal.ZERO;

    @Column(name = "late_penalty", nullable = false, precision = 7, scale = 2)
    private BigDecimal latePenalty = BigDecimal.ZERO;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal adjustment = BigDecimal.ZERO;

    @Column(name = "adjustment_reason")
    private String adjustmentReason;

    /** DB 계산 컬럼. 읽기 전용 */
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "final_score", precision = 7, scale = 2, insertable = false, updatable = false)
    private BigDecimal finalScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScoreStatus status = ScoreStatus.PROVISIONAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finalized_by")
    private User finalizedBy;

    @Column(name = "finalized_at")
    private Instant finalizedAt;

    /** 트리거가 갱신한다 */
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    public Score(Problem problem, User student) {
        this.problem = problem;
        this.student = student;
    }

    /** 새 제출이 채점되면 자동 점수를 갱신 (마지막 제출이 성적 대상) */
    public void applyAutoScore(Submission submission, BigDecimal autoScore) {
        this.submission = submission;
        this.autoScore = autoScore;
    }

    /** 교수 조정. 사유 필수 (S3) */
    public void adjust(BigDecimal amount, String reason, User professor) {
        if (amount.signum() != 0 && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("점수를 조정할 때는 사유가 필요합니다");
        }
        this.adjustment = amount;
        this.adjustmentReason = reason;
        this.finalizedBy = professor;
    }

    public void finalizeBy(User professor, Instant now) {
        this.status = ScoreStatus.FINAL;
        this.finalizedBy = professor;
        this.finalizedAt = now;
    }

    public boolean isFinal() {
        return status == ScoreStatus.FINAL;
    }
}
