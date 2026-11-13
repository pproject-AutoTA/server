package com.autota.server.domain.problem.controller;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.problem.dto.ProblemCreateRequest;
import com.autota.server.domain.problem.dto.ProblemCreateResponse;
import com.autota.server.domain.problem.dto.ProfessorProblemResponse;
import com.autota.server.domain.problem.service.ProfessorProblemService;
import com.autota.server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/professor")
public class ProfessorProblemController {
    private final ProfessorProblemService professorProblemService;

    // 문제 등록
    @PostMapping("/assignments/{assignmentId}/problems")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProblemCreateResponse> createProblem(@AuthenticationPrincipal AuthUser authUser,
                                                            @PathVariable("assignmentId") long assignmentId,
                                                            @Valid @RequestBody ProblemCreateRequest request) {
        return ApiResponse.created(professorProblemService.createProblem(assignmentId, request, authUser));
    }

    // 문제 상세(교수)
    @GetMapping("/problems/{problemId}")
    public ApiResponse<ProfessorProblemResponse> getProblem(@AuthenticationPrincipal AuthUser authUser,
                                                            @PathVariable("problemId") long problemId) {
        return ApiResponse.ok(professorProblemService.getProblem(problemId, authUser));
    }
}
