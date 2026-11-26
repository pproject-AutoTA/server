package com.autota.server.domain.course.dto;

import java.time.LocalDateTime;

public record ProfessorCourseResponse (
        Long id,
        String name,
        String semester,
        String joinCode,
        int studentCount,
        LocalDateTime createdAt
) {
}
