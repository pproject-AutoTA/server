package com.autota.server.domain.course.controller;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.course.dto.CourseResponse;
import com.autota.server.domain.course.service.CourseService;
import com.autota.server.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
public class CourseController {
    private final CourseService courseService;

    // 내 수업 목록 조회
    @GetMapping("/me")
    public ApiResponse<List<CourseResponse>> getMyCourse(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.ok(courseService.getMyCourse(authUser));
    }
}
