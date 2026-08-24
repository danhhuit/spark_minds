package com.sparkminds.library.auth.service;

import com.sparkminds.library.auth.dto.request.ChangeEmailRequest;
import com.sparkminds.library.auth.dto.request.VerifyEmailChangeRequest;
import com.sparkminds.library.auth.entity.EmailChangeVerification;
import com.sparkminds.library.auth.repository.EmailChangeVerificationRepository;
import com.sparkminds.library.auth.repository.RefreshTokenRepository;
import com.sparkminds.library.common.api.MessageResponse;
import com.sparkminds.library.common.exception.InvalidEmailChangeException;
import com.sparkminds.library.common.exception.ResourceAlreadyExistsException;
import com.sparkminds.library.mail.service.MailService;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailChangeService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private static final int MAX_ATTEMPTS = 5;

    private final UserAccountRepository userAccountRepository;
    private final EmailChangeVerificationRepository verificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedTokenService revokedTokenService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Transactional
    public MessageResponse requestEmailChange(
            Jwt jwt,
            ChangeEmailRequest request
    ) {
        Long userId = getUserId(jwt);

        UserAccount user = userAccountRepository
                .findById(userId)
                .orElseThrow(() ->
                        new InvalidEmailChangeException(
                                "Account does not exist"
                        )
                );

        String normalizedNewEmail = request.newEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (user.getEmail()
                .equalsIgnoreCase(normalizedNewEmail)) {
            throw new InvalidEmailChangeException(
                    "New email must be different "
                            + "from current email"
            );
        }

        ensureEmailAndUsernameAvailable(
                normalizedNewEmail,
                userId
        );

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        verificationRepository.invalidateUnusedCodes(
                userId,
                now
        );

        String verificationCode =
                String.format(
                        "%06d",
                        SECURE_RANDOM.nextInt(1_000_000)
                );

        EmailChangeVerification verification =
                new EmailChangeVerification();

        verification.setUser(user);
        verification.setNewEmail(normalizedNewEmail);
        verification.setCodeHash(
                passwordEncoder.encode(verificationCode)
        );
        verification.setExpiresAt(
                now.plusMinutes(10)
        );
        verification.setAttempts(0);
        verification.setUsed(false);

        verificationRepository.save(verification);

        mailService.sendEmailChangeCode(
                normalizedNewEmail,
                verificationCode
        );

        return new MessageResponse(
                "Verification code was sent "
                        + "to the new email."
        );
    }

    @Transactional(
        noRollbackFor = InvalidEmailChangeException.class
    )
    public MessageResponse verifyEmailChange(
            Jwt jwt,
            VerifyEmailChangeRequest request
    ) {
        Long userId = getUserId(jwt);

        EmailChangeVerification verification =
                verificationRepository
                    .findTopByUser_IdAndUsedFalseOrderByCreatedAtDesc(
                        userId
                    )
                    .orElseThrow(() ->
                        new InvalidEmailChangeException(
                            "No pending email change request"
                        )
                    );

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        if (!verification.getExpiresAt().isAfter(now)) {
            verification.setUsed(true);
            verification.setUsedAt(now);

            throw new InvalidEmailChangeException(
                    "Verification code has expired"
            );
        }

        if (verification.getAttempts() >= MAX_ATTEMPTS) {
            verification.setUsed(true);
            verification.setUsedAt(now);

            throw new InvalidEmailChangeException(
                    "Maximum verification attempts exceeded"
            );
        }

        if (!passwordEncoder.matches(
                request.code(),
                verification.getCodeHash()
        )) {
            int attempts =
                    verification.getAttempts() + 1;

            verification.setAttempts(attempts);

            if (attempts >= MAX_ATTEMPTS) {
                verification.setUsed(true);
                verification.setUsedAt(now);
            }

            throw new InvalidEmailChangeException(
                    "Verification code is incorrect"
            );
        }

        UserAccount user = verification.getUser();
        String oldEmail = user.getEmail();
        String newEmail = verification.getNewEmail();

        ensureEmailAndUsernameAvailable(
                newEmail,
                user.getId()
        );

        // Member username ban đầu chính là email.
        // Admin username "admin" sẽ không bị thay đổi.
        if (user.getUsername()
                .equalsIgnoreCase(oldEmail)) {
            user.setUsername(newEmail);
        }

        user.setEmail(newEmail);

        verification.setUsed(true);
        verification.setUsedAt(now);

        refreshTokenRepository.revokeAllActiveTokens(
                user.getId(),
                now
        );

        // Access token hiện tại chứa email cũ,
        // vì vậy blacklist để buộc login lại.
        revokedTokenService.revoke(
                jwt,
                user.getId()
        );

        return new MessageResponse(
                "Email changed successfully. "
                        + "Please login again."
        );
    }

    private void ensureEmailAndUsernameAvailable(
            String newEmail,
            Long currentUserId
    ) {
        userAccountRepository
                .findByEmailIgnoreCase(newEmail)
                .filter(user ->
                        !user.getId().equals(currentUserId)
                )
                .ifPresent(user -> {
                    throw new ResourceAlreadyExistsException(
                            "Email has already been registered"
                    );
                });

        userAccountRepository
                .findByUsernameIgnoreCase(newEmail)
                .filter(user ->
                        !user.getId().equals(currentUserId)
                )
                .ifPresent(user -> {
                    throw new ResourceAlreadyExistsException(
                            "Email cannot be used"
                    );
                });
    }

    private Long getUserId(Jwt jwt) {
        Number userIdClaim = jwt.getClaim("uid");

        if (userIdClaim == null) {
            throw new InvalidEmailChangeException(
                    "Authenticated user is invalid"
            );
        }

        return userIdClaim.longValue();
    }
}