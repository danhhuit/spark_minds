package com.sparkminds.library.auth.dto.response;

import java.time.Instant;
import java.time.OffsetDateTime;

public record TokenResponse(
        String tokenType,
        String accessToken,
        long expiresIn,
        Instant accessTokenExpiresAt,
        String refreshToken,
        OffsetDateTime refreshTokenExpiresAt
) {
}