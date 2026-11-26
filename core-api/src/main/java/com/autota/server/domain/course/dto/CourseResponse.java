package com.autota.server.domain.course.dto;

public record CourseResponse (
        Long id,
        String name,
        String semester,
        String professorName,
        String joinCode,
        int studentCount,
        int assignmentCount
) {
}
