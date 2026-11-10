package com.autota.server.course.dto;

import com.autota.server.course.entity.Course;

/** 교수의 개설 수업 목록 항목. 참여 코드를 포함한다. */
public record OwnedCourseResponse(
        Long id,
        String name,
        String semester,
        String joinCode,
        int studentCount,
        int assignmentCount
) {
    public static OwnedCourseResponse of(Course course, long studentCount, long assignmentCount) {
        return new OwnedCourseResponse(
                course.getId(),
                course.getTitle(),
                course.getSemester(),
                course.getJoinCode(),
                Math.toIntExact(studentCount),
                Math.toIntExact(assignmentCount)
        );
    }
}
