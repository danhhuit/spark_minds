package com.sparkminds.library.auth.service;

import com.sparkminds.library.auth.dto.request.RegisterRequest;
import com.sparkminds.library.auth.entity.EmailVerificationToken;
import com.sparkminds.library.auth.repository.EmailVerificationTokenRepository;
import com.sparkminds.library.common.api.MessageResponse;
import com.sparkminds.library.common.exception.InvalidVerificationTokenException;
import com.sparkminds.library.common.exception.ResourceAlreadyExistsException;
import com.sparkminds.library.mail.service.MailService;
import com.sparkminds.library.member.entity.MemberProfile;
import com.sparkminds.library.member.entity.Role;
import com.sparkminds.library.member.entity.RoleName;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.RoleRepository;
import com.sparkminds.library.member.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userAccountRepository
                .existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResourceAlreadyExistsException(
                    "Email has already been registered"
            );
        }

        Role userRole = roleRepository
                .findByName(RoleName.USER)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "USER role does not exist"
                        )
                );

        UserAccount user = new UserAccount();
        user.setUsername(normalizedEmail);
        user.setEmail(normalizedEmail);
        user.setPassword(
                passwordEncoder.encode(request.password())
        );
        user.setEnabled(false);
        user.setEmailVerified(false);
        user.setAccountNonLocked(true);
        user.addRole(userRole);

        MemberProfile profile = new MemberProfile();
        profile.setMembershipCode(generateMembershipCode());
        user.attachMemberProfile(profile);

        userAccountRepository.save(user);

        String rawToken = generateRawToken();

        EmailVerificationToken verificationToken =
                new EmailVerificationToken();

        verificationToken.setUser(user);
        verificationToken.setTokenHash(hash(rawToken));
        verificationToken.setExpiresAt(
                OffsetDateTime.now(ZoneOffset.UTC)
                        .plusHours(24)
        );

        tokenRepository.save(verificationToken);

        mailService.sendVerificationEmail(
                normalizedEmail,
                rawToken
        );

        return new MessageResponse(
                "Registration successful. "
                        + "Please verify your email."
        );
    }

    @Transactional
    public MessageResponse verifyEmail(String rawToken) {
        EmailVerificationToken token = tokenRepository
                .findByTokenHashAndUsedFalse(hash(rawToken))
                .orElseThrow(
                        InvalidVerificationTokenException::new
                );

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        if (!token.getExpiresAt().isAfter(now)) {
            throw new InvalidVerificationTokenException();
        }

        UserAccount user = token.getUser();
        user.setEmailVerified(true);
        user.setEnabled(true);

        token.setUsed(true);
        token.setUsedAt(now);

        return new MessageResponse(
                "Email verified successfully. "
                        + "You can now login."
        );
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String generateMembershipCode() {
        return "MBR-"
                + UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase(Locale.ROOT);
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