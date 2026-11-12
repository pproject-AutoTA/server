package com.autota.server.domain.assignment.dto;

import java.time.LocalDateTime;

public record ProfessorAssignmentResponse(
    Long id,
    Long courseId,
    String title,
    LocalDateTime dueAt,
    int problemCount
) {
}
