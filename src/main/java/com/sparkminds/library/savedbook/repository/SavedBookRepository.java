package com.sparkminds.library.savedbook.repository;

import com.sparkminds.library.savedbook.entity.SavedBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavedBookRepository
        extends JpaRepository<SavedBook, Long> {

    boolean existsByUser_IdAndBook_Id(
            Long userId,
            Long bookId
    );

    Optional<SavedBook> findByUser_IdAndBook_Id(
            Long userId,
            Long bookId
    );

    Page<SavedBook> findByUser_Id(
            Long userId,
            Pageable pageable
    );
}
