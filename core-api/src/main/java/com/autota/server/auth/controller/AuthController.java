package com.autota.server.auth.controller;

import com.autota.server.auth.dto.LoginRequest;
import com.autota.server.auth.dto.LoginResponse;
import com.autota.server.auth.service.AuthService;
import com.autota.server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ApiResponse.ok(
                authService.login(request.email(), request.password())
        );
    }
}
