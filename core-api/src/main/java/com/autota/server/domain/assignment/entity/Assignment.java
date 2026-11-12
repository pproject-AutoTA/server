package com.autota.server.domain.assignment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "assignments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** courses.id. 다른 도메인이라 연관관계 대신 id만 둔다. */
    @Column(nullable = false)
    private Long courseId;

    /** 1~100자. 길이 검증은 API(400)에서. */
    @Column(nullable = false)
    private String title;

    /** 마감. 이후 제출은 409(SUBMISSION-002). */
    @Column(nullable = false)
    private Instant dueAt;

    /** C1. 값이 있으면 마감 후 제출을 받고 감점한다. Must에서는 항상 null. */
    @Column(precision = 5, scale = 2)
    private BigDecimal latePenaltyRate;

    /** S3. 이 시각 이후 학생에게 점수를 공개한다. Must에서는 항상 null. */
    private Instant scoreReleaseAt;

    /** DB 기본값(now())이 채운다. 읽기 전용. */
    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Assignment(Long courseId, String title, Instant dueAt) {
        this.courseId = courseId;
        this.title = title;
        this.dueAt = dueAt;
    }
}
