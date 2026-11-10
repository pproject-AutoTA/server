package com.autota.server.course.repository;

/** 수업별 참여 학생 수 집계 결과. */
public record CourseStudentCount(Long courseId, long count) {
}
