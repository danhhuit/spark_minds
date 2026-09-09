package com.sparkminds.library.member.repository;

import com.sparkminds.library.member.entity.Role;
import com.sparkminds.library.member.entity.RoleName;
import com.sparkminds.library.member.entity.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

  Optional<UserAccount> findByUsernameIgnoreCase(String username);

  Optional<UserAccount> findByEmailIgnoreCase(String email);

  boolean existsByUsernameIgnoreCase(String username);

  boolean existsByEmailIgnoreCase(String email);

  long countDistinctByRoles_Name(RoleName roleName);

  @Query(
      """
            select user.authorizationVersion
            from UserAccount user
            where user.id = :userId
            """)
  Optional<Long> findAuthorizationVersionById(@Param("userId") Long userId);

  @Modifying
  @Query(
      """
            update UserAccount user
            set user.authorizationVersion =
                user.authorizationVersion + 1
            where :role member of user.roles
            """)
  int incrementAuthorizationVersionForRole(@Param("role") Role role);

  boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

  boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

  @EntityGraph(
      attributePaths = {
        "roles",
        "roles.permissions",
        "directPermissions",
        "deniedPermissions",
        "memberProfile"
      })
  Optional<UserAccount> findDetailedById(Long id);

  @EntityGraph(
      attributePaths = {"roles", "roles.permissions", "directPermissions", "deniedPermissions"})
  Optional<UserAccount> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);
}
