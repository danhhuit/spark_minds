package com.sparkminds.library.auth.service;

import com.sparkminds.library.auth.dto.request.LoginRequest;
import com.sparkminds.library.auth.dto.response.TokenResponse;
import com.sparkminds.library.security.jwt.JwtTokenService;
import com.sparkminds.library.security.jwt.JwtTokenService.GeneratedAccessToken;
import com.sparkminds.library.security.service.CustomUserPrincipal;
import com.sparkminds.library.member.entity.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final RevokedTokenService revokedTokenService;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken
                        .unauthenticated(
                            request.usernameOrEmail(),
                            request.password()
                        )
                );

        CustomUserPrincipal principal =
                (CustomUserPrincipal)
                        authentication.getPrincipal();

        RefreshTokenService.IssuedRefreshToken refresh =
                refreshTokenService.issueForUser(
                        principal.getId()
                );

        return createResponse(
                principal,
                refresh.value(),
                refresh.expiresAt()
        );
    }

    @Transactional
    public TokenResponse issueTokens(UserAccount user) {
        CustomUserPrincipal principal =
                CustomUserPrincipal.from(user);

        RefreshTokenService.IssuedRefreshToken refresh =
                refreshTokenService.issueForUser(
                        user.getId()
                );

        return createResponse(
                principal,
                refresh.value(),
                refresh.expiresAt()
        );
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        RefreshTokenService.RotatedRefreshToken rotated =
                refreshTokenService.rotate(rawRefreshToken);

        CustomUserPrincipal principal =
                CustomUserPrincipal.from(
                        rotated.user()
                );

        return createResponse(
                principal,
                rotated.value(),
                rotated.expiresAt()
        );
    }

    @Transactional
    public void logout(
            Jwt jwt,
            String rawRefreshToken
    ) {
        Number userIdClaim = jwt.getClaim("uid");
        Long userId = userIdClaim.longValue();

        refreshTokenService.revokeIfPresent(
                rawRefreshToken,
                userId
        );

        revokedTokenService.revoke(jwt, userId);
    }

    private TokenResponse createResponse(
            CustomUserPrincipal principal,
            String refreshToken,
            java.time.OffsetDateTime refreshExpiresAt
    ) {
        GeneratedAccessToken accessToken =
                jwtTokenService.generateAccessToken(
                        principal
                );

        long expiresIn = Math.max(
                0,
                Duration.between(
                        Instant.now(),
                        accessToken.expiresAt()
                ).toSeconds()
        );

        return new TokenResponse(
                "Bearer",
                accessToken.value(),
                expiresIn,
                accessToken.expiresAt(),
                refreshToken,
                refreshExpiresAt
        );
    }
}
