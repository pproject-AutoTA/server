package com.autota.server.domain.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

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

    /** DB 기본값(now())이 채운다. 읽기 전용. */
    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Course(Long professorId, String name, String semester, String joinCode) {
        this.professorId = professorId;
        this.name = name;
        this.semester = semester;
        this.joinCode = joinCode;
    }
}
