package com.autota.server.domain.assignment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    /** create()가 채운다. 이후 수정 불가. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private Assignment(Long courseId, String title, Instant dueAt, Instant createdAt) {
        this.courseId = courseId;
        this.title = title;
        this.dueAt = dueAt;
        this.createdAt = createdAt;
    }

    public static Assignment create(Long courseId, String title, Instant dueAt) {
        return new Assignment(courseId, title, dueAt, Instant.now());
    }
}
