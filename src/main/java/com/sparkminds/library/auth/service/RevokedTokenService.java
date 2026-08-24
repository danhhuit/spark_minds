package com.sparkminds.library.auth.service;

import com.sparkminds.library.auth.entity.RevokedToken;
import com.sparkminds.library.auth.repository.RevokedTokenRepository;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class RevokedTokenService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public void revoke(Jwt jwt, Long userId) {
        if (jwt.getId() == null
                || jwt.getExpiresAt() == null
                || revokedTokenRepository
                        .existsByJti(jwt.getId())) {
            return;
        }

        UserAccount user = userAccountRepository
                .getReferenceById(userId);

        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setJti(jwt.getId());
        revokedToken.setUser(user);
        revokedToken.setExpiresAt(
                OffsetDateTime.ofInstant(
                        jwt.getExpiresAt(),
                        ZoneOffset.UTC
                )
        );

        revokedTokenRepository.save(revokedToken);
    }
}