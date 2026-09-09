package com.sparkminds.library.auth.repository;

import com.sparkminds.library.auth.entity.AccessToken;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

  Optional<AccessToken> findByJtiAndUser_Id(String jti, Long userId);

  boolean existsByJtiAndUser_IdAndRevokedFalseAndExpiresAtAfter(
      String jti, Long userId, OffsetDateTime currentTime);

  @EntityGraph(attributePaths = "user")
  @Query("select token from AccessToken token")
  Page<AccessToken> findAllDetailed(Pageable pageable);
}
