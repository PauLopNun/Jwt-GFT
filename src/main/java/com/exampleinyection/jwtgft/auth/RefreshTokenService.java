package com.exampleinyection.jwtgft.auth;

import com.exampleinyection.jwtgft.user.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final long refreshTokenDurationMs;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(
        @Value("${application.security.jwt.refresh-token.expiration}") long refreshTokenDurationMs,
        RefreshTokenRepository refreshTokenRepository
    ) {
        this.refreshTokenDurationMs = refreshTokenDurationMs;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
            .orElseGet(() -> {
                RefreshToken token = new RefreshToken();
                token.setUser(user);
                return token;
            });
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(refreshTokenDurationMs, ChronoUnit.MILLIS));
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token expired. Please log in again.");
        }
        return token;
    }

    @Transactional
    public void revokeByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}

