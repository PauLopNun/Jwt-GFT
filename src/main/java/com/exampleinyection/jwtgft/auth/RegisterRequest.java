package com.exampleinyection.jwtgft.auth;

import com.exampleinyection.jwtgft.user.Role;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "email is required")
    String email,
    @NotBlank(message = "password is required")
    String password,
    Role role
) {
}

