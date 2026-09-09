package com.sparkminds.library.savedbook.repository;

import com.sparkminds.library.savedbook.entity.SavedBook;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedBookRepository extends JpaRepository<SavedBook, Long> {

  boolean existsByUser_IdAndBook_Id(Long userId, Long bookId);

  Optional<SavedBook> findByUser_IdAndBook_Id(Long userId, Long bookId);

  Page<SavedBook> findByUser_Id(Long userId, Pageable pageable);
}
