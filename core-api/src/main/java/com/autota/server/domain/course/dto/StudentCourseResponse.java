package com.autota.server.domain.course.dto;

public record StudentCourseResponse (
        Long courseId,
        String name,
        String semester,
        String professorName,
        java.time.LocalDateTime joinedAt
) {
}
