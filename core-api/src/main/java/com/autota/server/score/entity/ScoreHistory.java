package com.autota.server.score.entity;

import com.autota.server.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;

/** 공개 뒤 점수 수정 이력. DB 트리거가 쓰고, 앱은 읽기만 한다. */
@Entity
@Immutable
@Table(name = "score_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScoreHistory {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_id")
    private Score score;

    @Column(name = "old_final_score", precision = 7, scale = 2)
    private BigDecimal oldFinalScore;

    @Column(name = "new_final_score", precision = 7, scale = 2)
    private BigDecimal newFinalScore;

    @Column(name = "old_adjustment", precision = 7, scale = 2)
    private BigDecimal oldAdjustment;

    @Column(name = "new_adjustment", precision = 7, scale = 2)
    private BigDecimal newAdjustment;

    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "changed_at")
    private Instant changedAt;
}
