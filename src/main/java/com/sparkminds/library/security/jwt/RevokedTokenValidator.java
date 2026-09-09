package com.sparkminds.library.security.jwt;

import com.sparkminds.library.auth.repository.AccessTokenRepository;
import com.sparkminds.library.auth.repository.RevokedTokenRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import com.sparkminds.library.member.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RevokedTokenValidator implements OAuth2TokenValidator<Jwt> {

  private final RevokedTokenRepository revokedTokenRepository;
  private final AccessTokenRepository accessTokenRepository;
  private final UserAccountRepository userAccountRepository;

  @Override
  @Transactional(readOnly = true)
  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    String jti = jwt.getId();

    if (jti == null || jti.isBlank()) {
      return failure("JWT does not contain jti");
    }

    if (revokedTokenRepository.existsByJti(jti)) {
      return failure("JWT has been revoked");
    }

    Number userId = jwt.getClaim("uid");
    Number tokenAuthorizationVersion = jwt.getClaim("authv");
    if (userId == null || tokenAuthorizationVersion == null) {
      return failure("JWT does not contain authorization version");
    }

    boolean registeredAndActive =
        accessTokenRepository.existsByJtiAndUser_IdAndRevokedFalseAndExpiresAtAfter(
            jti, userId.longValue(), OffsetDateTime.now(ZoneOffset.UTC));
    if (!registeredAndActive) {
      return failure("JWT is not registered or is no longer active");
    }

    long currentAuthorizationVersion =
        userAccountRepository.findAuthorizationVersionById(userId.longValue()).orElse(-1L);

    if (currentAuthorizationVersion != tokenAuthorizationVersion.longValue()) {
      return failure("JWT permissions are stale");
    }

    return OAuth2TokenValidatorResult.success();
  }

  private OAuth2TokenValidatorResult failure(String description) {
    OAuth2Error error = new OAuth2Error("invalid_token", description, null);

    return OAuth2TokenValidatorResult.failure(error);
  }
}
