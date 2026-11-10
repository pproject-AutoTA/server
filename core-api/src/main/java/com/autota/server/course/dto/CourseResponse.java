package com.autota.server.course.dto;

import com.autota.server.course.entity.Course;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record CourseResponse(
        Long id,
        String name,
        String semester,
        String joinCode,
        long studentCount,
        LocalDateTime createdAt
) {
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    public static CourseResponse of(Course course, long studentCount) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getSemester(),
                course.getJoinCode(),
                studentCount,
                LocalDateTime.ofInstant(course.getCreatedAt(), ZONE)
        );
    }
}
