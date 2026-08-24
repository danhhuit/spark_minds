package com.sparkminds.library.auth.repository;

import com.sparkminds.library.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<EmailVerificationToken>
    findByTokenHashAndUsedFalse(String tokenHash);
}