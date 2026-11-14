package com.autota.server.domain.submission.controller;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.submission.dto.MySubmissionDetailResponse;
import com.autota.server.domain.submission.dto.MySubmissionsResponse;
import com.autota.server.domain.submission.dto.SubmitRequest;
import com.autota.server.domain.submission.dto.SubmitResponse;
import com.autota.server.domain.submission.service.StudentSubmissionService;
import com.autota.server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/problems/{problemId}/submissions/me")
    public ApiResponse<List<MySubmissionsResponse>> getMySubmissions(@AuthenticationPrincipal AuthUser user,
                                                                     @PathVariable("problemId") long problemId) {
        //내 제출 이력 조회 (문제 하나에 대한 제출 목록 최신 순 조회)
        return ApiResponse.ok(studentSubmissionService.getMySubmissions(user, problemId));
    }

    @GetMapping("/submissions/me/{submissionId}")
    public ApiResponse<MySubmissionDetailResponse> getMySubmission(@AuthenticationPrincipal AuthUser user,
                                @PathVariable("submissionId") long submissionId) {
        return ApiResponse.ok(studentSubmissionService.getMySubmission(user, submissionId));
    }
}
