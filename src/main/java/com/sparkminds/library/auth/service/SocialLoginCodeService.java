package com.sparkminds.library.auth.service;

import com.sparkminds.library.auth.dto.response.TokenResponse;
import com.sparkminds.library.auth.entity.SocialLoginCode;
import com.sparkminds.library.auth.repository.SocialLoginCodeRepository;
import com.sparkminds.library.common.exception.InvalidSocialLoginCodeException;
import com.sparkminds.library.member.entity.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class SocialLoginCodeService {

        private static final SecureRandom SECURE_RANDOM = new SecureRandom();

        private final SocialLoginCodeRepository repository;
        private final AuthService authService;

        @Transactional
        public String issueFor(UserAccount user) {
                byte[] bytes = new byte[32];
                SECURE_RANDOM.nextBytes(bytes);

                String rawCode = Base64.getUrlEncoder()
                                .withoutPadding()
                                .encodeToString(bytes);

                SocialLoginCode loginCode = new SocialLoginCode();
                loginCode.setUser(user);
                loginCode.setCodeHash(hash(rawCode));
                loginCode.setExpiresAt(
                                OffsetDateTime.now(ZoneOffset.UTC)
                                                .plusMinutes(2));
                loginCode.setUsed(false);

                repository.save(loginCode);
                return rawCode;
        }

        @Transactional
        public TokenResponse exchange(String rawCode) {
                SocialLoginCode loginCode = repository
                                .findByCodeHashAndUsedFalse(hash(rawCode))
                                .orElseThrow(
                                                InvalidSocialLoginCodeException::new);

                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

                if (!loginCode.getExpiresAt().isAfter(now)) {
                        throw new InvalidSocialLoginCodeException();
                }

                UserAccount user = loginCode.getUser();
                if (!user.isEnabled()
                                || !user.isEmailVerified()
                                || !user.isAccountNonLocked()) {
                        throw new InvalidSocialLoginCodeException();
                }

                loginCode.setUsed(true);
                loginCode.setUsedAt(now);

                return authService.issueTokens(user);
        }

        private String hash(String value) {
                try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");

                        return HexFormat.of().formatHex(
                                        digest.digest(
                                                        value.getBytes(
                                                                        StandardCharsets.UTF_8)));
                } catch (NoSuchAlgorithmException exception) {
                        throw new IllegalStateException(
                                        "SHA-256 is not available",
                                        exception);
                }
        }
}
