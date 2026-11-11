package com.autota.server.course.entity;

import com.autota.server.domain.user.entity.Role;
import com.autota.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/** 수업 참여 (M2). 학생이 참여 코드로 들어오면 1줄 생긴다. 참여 시각 = created_at. */
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** 학생이 수업에 참여. */
    public static Enrollment join(Course course, User student) {
        return new Enrollment(course, student);
    }

    public Enrollment(Course course, User student) {
        if (!student.is(Role.STUDENT)) {
            throw new IllegalArgumentException("학생만 수업에 참여할 수 있습니다");
        }
        this.course = course;
        this.student = student;
    }
}
