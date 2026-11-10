package com.autota.server.course.dto;

import com.autota.server.course.entity.Course;

/** 학생의 내 수업 목록 항목. 참여 코드는 포함하지 않는다. */
public record EnrolledCourseResponse(
        Long id,
        String name,
        String semester,
        String professorName,
        int studentCount,
        int assignmentCount
) {
    public static EnrolledCourseResponse of(Course course, String professorName, long studentCount, long assignmentCount) {
        return new EnrolledCourseResponse(
                course.getId(),
                course.getTitle(),
                course.getSemester(),
                professorName,
                Math.toIntExact(studentCount),
                Math.toIntExact(assignmentCount)
        );
    }
}
