package com.sparkminds.library.security.jwt;

import com.sparkminds.library.config.JwtProperties;
import com.sparkminds.library.security.service.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public GeneratedAccessToken generateAccessToken(
            CustomUserPrincipal principal
    ) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(
                jwtProperties.accessTokenMinutes(),
                ChronoUnit.MINUTES
        );

        String jti = UUID.randomUUID().toString();

        List<String> roles = principal
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(principal.getUsername())
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiresAt)
                .id(jti)
                .claim("uid", principal.getId())
                .claim("email", principal.getEmail())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        String token = jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                header,
                                claims
                        )
                )
                .getTokenValue();

        return new GeneratedAccessToken(
                token,
                jti,
                expiresAt
        );
    }

    public record GeneratedAccessToken(
            String value,
            String jti,
            Instant expiresAt
    ) {
    }
}