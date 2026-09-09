package com.sparkminds.library.auth.dto.response;

import java.time.OffsetDateTime;

public record TokenInfoResponse(
    Long id,
    String tokenType,
    String jti,
    String fingerprint,
    Long userId,
    String username,
    String email,
    OffsetDateTime issuedAt,
    OffsetDateTime expiresAt,
    String status,
    OffsetDateTime revokedAt) {}
