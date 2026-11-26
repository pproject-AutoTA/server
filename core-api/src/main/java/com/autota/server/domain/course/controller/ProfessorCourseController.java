package com.autota.server.domain.course.controller;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.course.dto.ProfessorCourseRequest;
import com.autota.server.domain.course.dto.ProfessorCourseResponse;
import com.autota.server.domain.course.service.ProfessorCourseService;
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
@RequestMapping("/api/v1/professor")
@RequiredArgsConstructor
public class ProfessorCourseController {
    private final ProfessorCourseService professorCourseService;

    // 1.수업 개설
    @PostMapping("courses")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProfessorCourseResponse> createCourse(@AuthenticationPrincipal AuthUser authUser,
                                                             @Valid @RequestBody ProfessorCourseRequest request) {
        return ApiResponse.created(
                professorCourseService.createCourse(authUser.id(), request)
        );
    }
}
