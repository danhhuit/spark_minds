package com.sparkminds.library.auth.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import com.sparkminds.library.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository
                extends JpaRepository<RefreshToken, Long> {

        @EntityGraph(attributePaths = { "user", "user.roles" })
        Optional<RefreshToken> findByTokenHashAndRevokedFalse(
                        String tokenHash);

        @Modifying
        @Query("""
                        update RefreshToken token
                           set token.revoked = true,
                               token.revokedAt = :revokedAt
                         where token.user.id = :userId
                           and token.revoked = false
                        """)
        int revokeAllActiveTokens(
                        @Param("userId") Long userId,
                        @Param("revokedAt") OffsetDateTime revokedAt);
}
