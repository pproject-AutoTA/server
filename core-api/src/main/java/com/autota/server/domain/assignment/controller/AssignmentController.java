package com.autota.server.domain.assignment.controller;

import com.autota.server.domain.assignment.dto.AssignmentResponse;
import com.autota.server.domain.assignment.service.AssignmentService;
import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;

    // 과제 및 문제 목록 조회
    @GetMapping("/courses/{courseId}/assignments")
    public ApiResponse<List<AssignmentResponse>> getAssignments(@AuthenticationPrincipal AuthUser authUser,
                                                                @PathVariable Long courseId) {
        return ApiResponse.ok(assignmentService.getAssignments(courseId, authUser));
    }
}
