package com.sparkminds.library.member.repository;

import com.sparkminds.library.member.entity.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository
        extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCaseAndIdNot(
            String username,
            Long id
    );

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );

    @EntityGraph(attributePaths = {
            "roles",
            "memberProfile"
    })
    Optional<UserAccount> findDetailedById(Long id);

    @EntityGraph(attributePaths = "roles")
    Optional<UserAccount> findByUsernameIgnoreCaseOrEmailIgnoreCase(
            String username,
            String email
    );
}
