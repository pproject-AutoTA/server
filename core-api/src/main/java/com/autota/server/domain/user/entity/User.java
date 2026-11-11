package com.autota.server.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String studentNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    /** DB 기본값(now())이 채운다. 읽기 전용. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
