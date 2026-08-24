package com.sparkminds.library.auth.repository;

import com.sparkminds.library.auth.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, Long> {

    boolean existsByJti(String jti);
}