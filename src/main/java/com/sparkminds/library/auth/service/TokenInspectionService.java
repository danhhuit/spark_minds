package com.sparkminds.library.auth.service;

import com.sparkminds.library.auth.dto.response.TokenInfoResponse;
import com.sparkminds.library.auth.entity.AccessToken;
import com.sparkminds.library.auth.entity.RefreshToken;
import com.sparkminds.library.auth.repository.AccessTokenRepository;
import com.sparkminds.library.auth.repository.RefreshTokenRepository;
import com.sparkminds.library.common.api.PageResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenInspectionService {

  private final AccessTokenRepository accessTokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional(readOnly = true)
  public PageResponse<TokenInfoResponse> getAccessTokens(int page, int size) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    PageRequest pageable =
        PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));

    return PageResponse.from(
        accessTokenRepository.findAllDetailed(pageable).map(token -> toResponse(token, now)));
  }

  @Transactional(readOnly = true)
  public PageResponse<TokenInfoResponse> getRefreshTokens(int page, int size) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    PageRequest pageable =
        PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));

    return PageResponse.from(
        refreshTokenRepository.findAllDetailed(pageable).map(token -> toResponse(token, now)));
  }

  private TokenInfoResponse toResponse(AccessToken token, OffsetDateTime now) {
    String status;
    if (token.isRevoked()) {
      status = "REVOKED";
    } else if (!token.getExpiresAt().isAfter(now)) {
      status = "EXPIRED";
    } else if (token.getAuthorizationVersion() != token.getUser().getAuthorizationVersion()) {
      status = "STALE";
    } else {
      status = "ACTIVE";
    }

    return new TokenInfoResponse(
        token.getId(),
        "ACCESS",
        token.getJti(),
        fingerprint(token.getTokenHash()),
        token.getUser().getId(),
        token.getUser().getUsername(),
        token.getUser().getEmail(),
        token.getIssuedAt(),
        token.getExpiresAt(),
        status,
        token.getRevokedAt());
  }

  private TokenInfoResponse toResponse(RefreshToken token, OffsetDateTime now) {
    String status =
        token.isRevoked()
            ? "REVOKED"
            : token.getExpiresAt().isAfter(now) ? "ACTIVE" : "EXPIRED";

    return new TokenInfoResponse(
        token.getId(),
        "REFRESH",
        null,
        fingerprint(token.getTokenHash()),
        token.getUser().getId(),
        token.getUser().getUsername(),
        token.getUser().getEmail(),
        token.getCreatedAt(),
        token.getExpiresAt(),
        status,
        token.getRevokedAt());
  }

  private String fingerprint(String tokenHash) {
    return tokenHash == null || tokenHash.length() <= 12
        ? tokenHash
        : tokenHash.substring(0, 12) + "...";
  }
}
