package com.autota.server.course.entity;

import com.autota.server.auth.entity.Role;
import com.autota.server.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.security.SecureRandom;
import java.time.Instant;

/** 수업 (M2). 교수 1명 + 학생 N명. 참여 코드로 들어온다. */
@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private User professor;

    @Column(nullable = false)
    private String title;

    /** 형식 DS-7K92-QX */
    @Column(name = "join_code", nullable = false, unique = true)
    private String joinCode;

    @Column(nullable = false)
    private String semester;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Course(User professor, String title, String joinCode, String semester) {
        if (!professor.is(Role.PROFESSOR)) {
            throw new IllegalArgumentException("수업은 교수만 만들 수 있습니다");
        }
        this.professor = professor;
        this.title = title;
        this.joinCode = joinCode;
        this.semester = semester;
    }

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALNUM = LETTERS + "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /** 새 수업 생성. 참여 코드는 자동으로 만든다. */
    public static Course create(User professor, String title, String semester) {
        return new Course(professor, title, generateJoinCode(), semester);
    }

    /** 참여 코드를 지정해 수업 생성. 형식(DS-7K92-QX)이 아니면 예외. */
    public static Course create(User professor, String title, String joinCode, String semester) {
        if (joinCode == null || !joinCode.matches("^[A-Z]{2}-[A-Z0-9]{4}-[A-Z]{2}$")) {
            throw new IllegalArgumentException("참여 코드 형식이 올바르지 않습니다");
        }
        return new Course(professor, title, joinCode, semester);
    }

    /** 참여 코드 생성. 형식은 DS-7K92-QX (영문 2자 - 영문·숫자 4자 - 영문 2자). */
    public static String generateJoinCode() {
        return randomChars(LETTERS, 2) + "-" + randomChars(ALNUM, 4) + "-" + randomChars(LETTERS, 2);
    }

    private static String randomChars(String chars, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public boolean isOwnedBy(User user) {
        return professor.getId().equals(user.getId());
    }
}
