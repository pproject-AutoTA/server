package com.autota.server.assignment.entity;

import com.autota.server.course.entity.Course;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * 과제. 수업 안의 문제 묶음. 마감과 점수 공개는 과제 단위.
 * 실제 공개(미확정 점수 → 확정)는 DB 함수 release_assignment_scores()를 부른다.
 */
@Entity
@Table(name = "assignments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "score_release", nullable = false)
    private ScoreRelease scoreRelease = ScoreRelease.HIDDEN;

    @Column(name = "score_release_at")
    private Instant scoreReleaseAt;     // 예약 시각

    @Column(name = "score_released_at")
    private Instant scoreReleasedAt;    // 실제 공개 시각

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Assignment(Course course, String title, String description, Instant dueAt) {
        this.course = course;
        this.title = title;
        this.description = description;
        this.dueAt = dueAt;
    }

    public boolean isPastDue(Instant now) {
        return now.isAfter(dueAt);
    }

    /** 비공개 케이스별 결과는 마감 후에만 학생에게 보인다 (6절) */
    public boolean canShowHiddenCaseResults(Instant now) {
        return isPastDue(now);
    }

    public boolean isScoreReleased() {
        return scoreRelease == ScoreRelease.RELEASED;
    }

    /** 예약 공개 (S3). 시각이 되면 스케줄러가 release_assignment_scores()를 부른다 */
    public void scheduleRelease(Instant at) {
        if (isScoreReleased()) {
            throw new IllegalStateException("이미 공개된 과제입니다");
        }
        this.scoreRelease = ScoreRelease.SCHEDULED;
        this.scoreReleaseAt = at;
    }

    public void cancelScheduledRelease() {
        if (scoreRelease == ScoreRelease.SCHEDULED) {
            this.scoreRelease = ScoreRelease.HIDDEN;
            this.scoreReleaseAt = null;
        }
    }
}
