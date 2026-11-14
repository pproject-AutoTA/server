package com.autota.server.domain.submission.controller;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.submission.dto.SubmitRequest;
import com.autota.server.domain.submission.dto.SubmitResponse;
import com.autota.server.domain.submission.service.StudentSubmissionService;
import com.autota.server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StudentSubmissionController {
    private final StudentSubmissionService studentSubmissionService;

    @PostMapping("/problems/{problemId}/submissions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<SubmitResponse> submit(@AuthenticationPrincipal AuthUser user,
                                              @PathVariable("problemId") long problemId,
                                              @Valid @RequestBody SubmitRequest request) {
        return ApiResponse.accepted(studentSubmissionService.submit(user, problemId, request.code()));
    }

}
