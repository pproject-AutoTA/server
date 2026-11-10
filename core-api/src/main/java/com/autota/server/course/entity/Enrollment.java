package com.autota.server.course.entity;

import com.autota.server.auth.entity.Role;
import com.autota.server.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/** 수업 참여 (M2). 학생이 참여 코드로 들어오면 1줄 생긴다. */
@Entity
@Table(name = "enrollments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {

    @EmbeddedId
    private EnrollmentId id;

    @MapsId("courseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    /** 학생이 수업에 참여. 수업이 저장돼 id가 있어야 한다. */
    public static Enrollment join(Course course, User student) {
        return new Enrollment(course, student);
    }

    public Enrollment(Course course, User student) {
        if (!student.is(Role.STUDENT)) {
            throw new IllegalArgumentException("학생만 수업에 참여할 수 있습니다");
        }
        this.id = EnrollmentId.of(course.getId(), student.getId());
        this.course = course;
        this.student = student;
    }
}
