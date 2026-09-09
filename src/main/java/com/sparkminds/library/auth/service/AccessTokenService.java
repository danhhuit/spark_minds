package com.sparkminds.library.auth.service;

import com.sparkminds.library.auth.entity.AccessToken;
import com.sparkminds.library.auth.repository.AccessTokenRepository;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.UserAccountRepository;
import com.sparkminds.library.security.jwt.JwtTokenService.GeneratedAccessToken;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

  private final AccessTokenRepository accessTokenRepository;
  private final UserAccountRepository userAccountRepository;
  private final TokenHashService tokenHashService;

  @Transactional
  public void record(Long userId, long authorizationVersion, GeneratedAccessToken generated) {
    UserAccount user = userAccountRepository.getReferenceById(userId);

    AccessToken token = new AccessToken();
    token.setJti(generated.jti());
    token.setUser(user);
    token.setTokenHash(tokenHashService.hash(generated.value()));
    token.setAuthorizationVersion(authorizationVersion);
    token.setIssuedAt(OffsetDateTime.ofInstant(generated.issuedAt(), ZoneOffset.UTC));
    token.setExpiresAt(OffsetDateTime.ofInstant(generated.expiresAt(), ZoneOffset.UTC));
    token.setRevoked(false);

    accessTokenRepository.save(token);
  }

  @Transactional
  public void revoke(String jti, Long userId) {
    if (jti == null || jti.isBlank()) {
      return;
    }
    accessTokenRepository
        .findByJtiAndUser_Id(jti, userId)
        .filter(token -> !token.isRevoked())
        .ifPresent(
            token -> {
              token.setRevoked(true);
              token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
            });
  }
}
