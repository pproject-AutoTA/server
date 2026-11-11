package com.autota.server.course.controller;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.course.dto.CourseResponse;
import com.autota.server.course.dto.CreateCourseRequest;
import com.autota.server.course.dto.EnrolledCourseResponse;
import com.autota.server.course.dto.JoinCourseRequest;
import com.autota.server.course.dto.JoinCourseResponse;
import com.autota.server.course.dto.OwnedCourseResponse;
import com.autota.server.course.entity.Course;
import com.autota.server.course.service.CourseService;
import com.autota.server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CourseController {
    private final CourseService courseService;

    //수업 참여 (학생, COURSE-02)
    @PostMapping("/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<JoinCourseResponse> joinCourse(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody JoinCourseRequest request
    ) {
        return ApiResponse.created(courseService.joinCourse(user.id(), request.joinCode()));
    }

    //내 수업 목록 조회 (학생, COURSE-03)
    @GetMapping("/courses/me")
    public ApiResponse<List<EnrolledCourseResponse>> getEnrolledCourses(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(courseService.getEnrolledCourses(user.id()));
    }

    //개설 수업 목록 조회 (교수, COURSE-03)
    @GetMapping("/professor/courses")
    public ApiResponse<List<OwnedCourseResponse>> getOwnedCourses(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(courseService.getOwnedCourses(user.id()));
    }

    //수업 개설 (교수, COURSE-01)
    @PostMapping("/professor/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CourseResponse> createCourse(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody CreateCourseRequest request
    ) {
        Course course = courseService.createCourse(user.id(), request.name(), request.semester());
        return ApiResponse.created(CourseResponse.of(course, 0)); // 새 수업이라 학생 0명
    }
}
