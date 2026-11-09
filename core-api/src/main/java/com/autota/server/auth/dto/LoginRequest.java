package com.autota.server.auth.dto;

import com.autota.server.auth.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotNull Role role
) {
}
