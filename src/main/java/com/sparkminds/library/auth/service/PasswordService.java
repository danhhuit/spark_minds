package com.sparkminds.library.auth.service;

import com.sparkminds.library.auth.dto.request.ChangePasswordRequest;
import com.sparkminds.library.auth.dto.request.ResetPasswordRequest;
import com.sparkminds.library.auth.entity.PasswordResetToken;
import com.sparkminds.library.auth.repository.PasswordResetTokenRepository;
import com.sparkminds.library.auth.repository.RefreshTokenRepository;
import com.sparkminds.library.common.api.MessageResponse;
import com.sparkminds.library.common.exception.CurrentPasswordMismatchException;
import com.sparkminds.library.common.exception.InvalidPasswordResetTokenException;
import com.sparkminds.library.common.exception.PasswordReuseException;
import com.sparkminds.library.mail.service.MailService;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
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
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final UserAccountRepository userAccountRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedTokenService revokedTokenService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Transactional
    public MessageResponse requestPasswordReset(String email) {
        String normalizedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);

        userAccountRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .filter(UserAccount::isEmailVerified)
                .filter(UserAccount::isEnabled)
                .ifPresent(user -> {
                    OffsetDateTime now =
                            OffsetDateTime.now(ZoneOffset.UTC);

                    resetTokenRepository
                            .markUnusedTokensAsUsed(
                                    user.getId(),
                                    now
                            );

                    String rawToken = generateRawToken();

                    PasswordResetToken token =
                            new PasswordResetToken();

                    token.setUser(user);
                    token.setTokenHash(hash(rawToken));
                    token.setExpiresAt(
                            now.plusMinutes(30)
                    );

                    resetTokenRepository.save(token);

                    mailService.sendPasswordResetEmail(
                            user.getEmail(),
                            rawToken
                    );
                });

        // Không tiết lộ email có tồn tại hay không.
        return new MessageResponse(
                "If the email exists, "
                        + "a password reset email was sent."
        );
    }

    @Transactional
    public MessageResponse resetPassword(
            ResetPasswordRequest request
    ) {
        PasswordResetToken resetToken =
                resetTokenRepository
                    .findByTokenHashAndUsedFalse(
                        hash(request.token())
                    )
                    .orElseThrow(
                        InvalidPasswordResetTokenException::new
                    );

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        if (!resetToken.getExpiresAt().isAfter(now)) {
            throw new InvalidPasswordResetTokenException();
        }

        UserAccount user = resetToken.getUser();

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPassword()
        )) {
            throw new PasswordReuseException();
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        resetToken.setUsed(true);
        resetToken.setUsedAt(now);

        refreshTokenRepository.revokeAllActiveTokens(
                user.getId(),
                now
        );

        return new MessageResponse(
                "Password reset successfully. "
                        + "Please login again."
        );
    }

    @Transactional
    public MessageResponse changePassword(
            Jwt jwt,
            ChangePasswordRequest request
    ) {
        Number userIdClaim = jwt.getClaim("uid");
        Long userId = userIdClaim.longValue();

        UserAccount user = userAccountRepository
                .findById(userId)
                .orElseThrow(
                        CurrentPasswordMismatchException::new
                );

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword()
        )) {
            throw new CurrentPasswordMismatchException();
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPassword()
        )) {
            throw new PasswordReuseException();
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        refreshTokenRepository.revokeAllActiveTokens(
                userId,
                now
        );

        revokedTokenService.revoke(jwt, userId);

        return new MessageResponse(
                "Password changed successfully. "
                        + "Please login again."
        );
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                    digest.digest(
                        value.getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }
}