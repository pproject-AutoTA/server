package com.autota.server.assignment.repository;

/** 수업별 과제 수 집계 결과. */
public record CourseAssignmentCount(Long courseId, long count) {
}
