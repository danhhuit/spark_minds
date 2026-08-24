package com.sparkminds.library.member.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import com.sparkminds.library.member.entity.MemberProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberProfileRepository
                extends JpaRepository<MemberProfile, Long>,
                JpaSpecificationExecutor<MemberProfile> {

        Optional<MemberProfile> findByUser_Id(Long userId);

        boolean existsByMembershipCodeIgnoreCase(
                        String membershipCode);

        @EntityGraph(attributePaths = { "user", "user.roles" })
        @Query("""
                        select profile
                          from MemberProfile profile
                         where profile.id = :id
                        """)
        Optional<MemberProfile> findDetailedById(
                        @Param("id") Long id);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @EntityGraph(attributePaths = { "user", "user.roles" })
        @Query("""
                        select profile
                          from MemberProfile profile
                         where profile.user.id = :userId
                        """)
        Optional<MemberProfile> findByUserIdForUpdate(
                        @Param("userId") Long userId);
}