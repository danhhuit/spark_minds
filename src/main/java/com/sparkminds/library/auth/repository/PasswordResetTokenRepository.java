package com.sparkminds.library.auth.repository;

import com.sparkminds.library.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<PasswordResetToken>
    findByTokenHashAndUsedFalse(String tokenHash);

    @Modifying
    @Query("""
        update PasswordResetToken token
           set token.used = true,
               token.usedAt = :usedAt
         where token.user.id = :userId
           and token.used = false
        """)
    int markUnusedTokensAsUsed(
            @Param("userId") Long userId,
            @Param("usedAt") OffsetDateTime usedAt
    );
}