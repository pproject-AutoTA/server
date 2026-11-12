package com.autota.server.domain.course.controller;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.course.dto.StudentCourseRequest;
import com.autota.server.domain.course.dto.StudentCourseResponse;
import com.autota.server.domain.course.service.StudentCourseService;
import com.autota.server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StudentCourseController {
    private final StudentCourseService studentCourseService;

    //1. 수업 참여
    @PostMapping("/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StudentCourseResponse> enrollCourse(@AuthenticationPrincipal AuthUser authUser,
                                                           @Valid @RequestBody StudentCourseRequest request) {
        return ApiResponse.created(studentCourseService.enrollCourse(authUser, request));
    }
}
