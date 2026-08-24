package com.sparkminds.library.borrowing.repository;

import com.sparkminds.library.borrowing.entity.Borrowing;
import com.sparkminds.library.borrowing.entity.BorrowingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BorrowingRepository
        extends JpaRepository<Borrowing, Long> {

    boolean existsByMember_IdAndStatus(
            Long memberId,
            BorrowingStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select borrowing
          from Borrowing borrowing
         where borrowing.id = :id
        """)
    Optional<Borrowing> findByIdForUpdate(
            @Param("id") Long id
    );

    @EntityGraph(
        attributePaths = {
            "member",
            "member.user",
            "book"
        }
    )
    Page<Borrowing> findByMember_User_Id(
            Long userId,
            Pageable pageable
    );

    @EntityGraph(
        attributePaths = {
            "member",
            "member.user",
            "book"
        }
    )
    @Query("select borrowing from Borrowing borrowing")
    Page<Borrowing> findAllDetailed(
            Pageable pageable
    );
}