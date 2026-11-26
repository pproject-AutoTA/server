package com.autota.server.domain.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "student_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** users.id. 다른 도메인이라 연관관계 대신 id만 둔다. */
    @Column(nullable = false)
    private Long studentId;

    /** 참여 시각(joinedAt). create()가 채운다. 이후 수정 불가. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private Enrollment(Course course, Long studentId, Instant createdAt) {
        this.course = course;
        this.studentId = studentId;
        this.createdAt = createdAt;
    }

    public static Enrollment create(Course course, Long studentId) {
        return new Enrollment(course, studentId, Instant.now());
    }
}
