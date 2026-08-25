package com.sparkminds.library.auth.repository;

import com.sparkminds.library.auth.entity.OAuthIdentity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthIdentityRepository
        extends JpaRepository<OAuthIdentity, Long> {

    @EntityGraph(attributePaths = {
            "user",
            "user.roles",
            "user.memberProfile"
    })
    Optional<OAuthIdentity> findByProviderAndProviderSubject(
            String provider,
            String providerSubject);

    Optional<OAuthIdentity> findByUser_IdAndProvider(
            Long userId,
            String provider);

    boolean existsByUser_IdAndProvider(
            Long userId,
            String provider);
}