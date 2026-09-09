package com.sparkminds.library.auth.repository;

import com.sparkminds.library.auth.entity.RefreshToken;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  @EntityGraph(attributePaths = {"user", "user.roles"})
  Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

  @EntityGraph(attributePaths = "user")
  @Query("select token from RefreshToken token")
  Page<RefreshToken> findAllDetailed(Pageable pageable);

  @Modifying
  @Query(
      """
                        update RefreshToken token
                           set token.revoked = true,
                               token.revokedAt = :revokedAt
                         where token.user.id = :userId
                           and token.revoked = false
                        """)
  int revokeAllActiveTokens(
      @Param("userId") Long userId, @Param("revokedAt") OffsetDateTime revokedAt);
}
