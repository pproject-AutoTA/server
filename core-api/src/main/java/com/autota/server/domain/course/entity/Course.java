package com.autota.server.domain.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** users.id. 다른 도메인이라 연관관계 대신 id만 둔다(교수 이름은 UserService로 조회). */
    @Column(nullable = false)
    private Long professorId;

    @Column(nullable = false)
    private String name;

    /** YYYY-N. 형식 검증은 API(400)에서. */
    @Column(nullable = false)
    private String semester;

    @Column(nullable = false, unique = true, length = 16)
    private String joinCode;

    /** create()가 채운다. 이후 수정 불가. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private Course(Long professorId, String name, String semester, String joinCode, Instant createdAt) {
        this.professorId = professorId;
        this.name = name;
        this.semester = semester;
        this.joinCode = joinCode;
        this.createdAt = createdAt;
    }

    public static Course create(Long professorId, String name, String semester, String joinCode) {
        return new Course(professorId, name, semester, joinCode, Instant.now());
    }
}
