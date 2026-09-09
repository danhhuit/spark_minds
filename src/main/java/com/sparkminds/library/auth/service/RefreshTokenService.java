package com.sparkminds.library.auth.service;

import com.sparkminds.library.auth.entity.RefreshToken;
import com.sparkminds.library.auth.repository.RefreshTokenRepository;
import com.sparkminds.library.common.exception.InvalidRefreshTokenException;
import com.sparkminds.library.config.TimeToLiveProperties;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.UserAccountRepository;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final RefreshTokenRepository refreshTokenRepository;
  private final UserAccountRepository userAccountRepository;
  private final TimeToLiveProperties timeToLiveProperties;
  private final TokenHashService tokenHashService;

  @Transactional
  public IssuedRefreshToken issueForUser(Long userId) {
    UserAccount user = userAccountRepository.getReferenceById(userId);

    return createToken(user);
  }

  @Transactional
  public RotatedRefreshToken rotate(String rawToken) {
    String tokenHash = tokenHashService.hash(rawToken);

    RefreshToken currentToken =
        refreshTokenRepository
            .findByTokenHashAndRevokedFalse(tokenHash)
            .orElseThrow(InvalidRefreshTokenException::new);

    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    if (!currentToken.getExpiresAt().isAfter(now)) {
      throw new InvalidRefreshTokenException();
    }

    UserAccount user = currentToken.getUser();

    if (!user.isEnabled() || !user.isEmailVerified() || !user.isAccountNonLocked()) {
      throw new InvalidRefreshTokenException();
    }

    currentToken.setRevoked(true);
    currentToken.setRevokedAt(now);

    IssuedRefreshToken replacement = createToken(user);

    return new RotatedRefreshToken(user, replacement.value(), replacement.expiresAt());
  }

  @Transactional
  public void revokeIfPresent(String rawToken, Long userId) {
    String tokenHash = tokenHashService.hash(rawToken);

    refreshTokenRepository
        .findByTokenHashAndRevokedFalse(tokenHash)
        .filter(token -> token.getUser().getId().equals(userId))
        .ifPresent(
            token -> {
              token.setRevoked(true);
              token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
            });
  }

  private IssuedRefreshToken createToken(UserAccount user) {
    byte[] randomBytes = new byte[48];
    SECURE_RANDOM.nextBytes(randomBytes);

    String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

    OffsetDateTime expiresAt =
        OffsetDateTime.now(ZoneOffset.UTC).plus(timeToLiveProperties.refreshToken());

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setTokenHash(tokenHashService.hash(rawToken));
    refreshToken.setExpiresAt(expiresAt);
    refreshToken.setRevoked(false);

    refreshTokenRepository.save(refreshToken);

    return new IssuedRefreshToken(rawToken, expiresAt);
  }

  public record IssuedRefreshToken(String value, OffsetDateTime expiresAt) {}

  public record RotatedRefreshToken(UserAccount user, String value, OffsetDateTime expiresAt) {}
}
