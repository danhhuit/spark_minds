package com.sparkminds.library.auth.repository;

import com.sparkminds.library.auth.entity.SocialLoginCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface SocialLoginCodeRepository
        extends JpaRepository<SocialLoginCode, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "user",
            "user.roles"
    })
    Optional<SocialLoginCode> findByCodeHashAndUsedFalse(
            String codeHash
    );
}
