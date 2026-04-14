package com.exampleinyection.jwtgft.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String BASE64_SECRET = "TWFudWFsTW9kNV9Nb2Q2X0pXVF9TZWNyZXRfSFMyNTZfMzJfYnl0ZXM=";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(BASE64_SECRET, 900_000L);
    }

    @Test
    void generateTokenValidUserReturnsSignedJwt() {
        UserDetails userDetails = User.withUsername("alice@bookstore.dev")
            .password("irrelevant")
            .roles("USER")
            .build();

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@bookstore.dev");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void extractUsernameExpiredTokenThrowsException() {
        JwtService expiredJwtService = new JwtService(BASE64_SECRET, -1_000L);
        UserDetails userDetails = User.withUsername("alice@bookstore.dev")
            .password("x")
            .roles("USER")
            .build();

        String token = expiredJwtService.generateToken(userDetails);

        assertThatThrownBy(() -> expiredJwtService.extractUsername(token))
            .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }
}

