package com.autota.server.domain.problem.controller;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.problem.dto.StudentProblemResponse;
import com.autota.server.domain.problem.service.StudentProblemService;
import com.autota.server.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class StudentProblemController {
    private final StudentProblemService studentProblemService;

    // 문제 상세(학생)
    @GetMapping("/problems/{problemId}")
    public ApiResponse<StudentProblemResponse> getProblem(@AuthenticationPrincipal AuthUser authUser,
                                                          @PathVariable("problemId") long problemId) {
        return ApiResponse.ok(studentProblemService.getProblem(problemId, authUser));
    }
}
