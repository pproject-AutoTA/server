package com.autota.server.course.dto;

import com.autota.server.course.entity.Course;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record JoinCourseResponse(
        Long courseId,
        String name,
        String semester,
        String professorName,
        LocalDateTime joinedAt
) {
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    public static JoinCourseResponse of(Course course, String professorName, Instant joinedAt) {
        return new JoinCourseResponse(
                course.getId(),
                course.getName(),
                course.getSemester(),
                professorName,
                LocalDateTime.ofInstant(joinedAt, ZONE)
        );
    }
}
