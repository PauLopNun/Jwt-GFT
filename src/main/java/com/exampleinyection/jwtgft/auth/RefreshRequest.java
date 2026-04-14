package com.exampleinyection.jwtgft.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank(message = "refreshToken is required")
    String refreshToken
) {
}

