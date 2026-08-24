package com.sparkminds.library.auth.repository;

import com.sparkminds.library.auth.entity.EmailChangeVerification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface EmailChangeVerificationRepository
        extends JpaRepository<EmailChangeVerification, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<EmailChangeVerification>
    findTopByUser_IdAndUsedFalseOrderByCreatedAtDesc(
            Long userId
    );

    @Modifying
    @Query("""
        update EmailChangeVerification verification
           set verification.used = true,
               verification.usedAt = :usedAt
         where verification.user.id = :userId
           and verification.used = false
        """)
    int invalidateUnusedCodes(
            @Param("userId") Long userId,
            @Param("usedAt") OffsetDateTime usedAt
    );
}