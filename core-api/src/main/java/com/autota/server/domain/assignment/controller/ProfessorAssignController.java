package com.autota.server.domain.assignment.controller;

import com.autota.server.domain.assignment.dto.AssignmentRequest;
import com.autota.server.domain.assignment.dto.ProfessorAssignmentResponse;
import com.autota.server.domain.assignment.service.ProfessorAssignService;
import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProfessorAssignController {
    private final ProfessorAssignService professorAssignService;

    @PostMapping("/professor/courses/{courseId}/assignments")
    public ApiResponse<ProfessorAssignmentResponse> assignCourse(@AuthenticationPrincipal AuthUser authUser,
                                                                 @Valid @RequestBody AssignmentRequest request,
                                                                 @PathVariable Long courseId) {
        return ApiResponse.created(
                professorAssignService.assignCourse(request, courseId, authUser)
        );
    }
}
