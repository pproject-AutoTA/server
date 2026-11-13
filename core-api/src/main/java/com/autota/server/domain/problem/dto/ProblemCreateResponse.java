package com.autota.server.domain.problem.dto;

public record ProblemCreateResponse(
        Long id,
        Long assignmentId,
        String title,
        int testcaseCount
) {
}
