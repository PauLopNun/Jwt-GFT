package com.exampleinyection.jwtgft.auth;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
}

