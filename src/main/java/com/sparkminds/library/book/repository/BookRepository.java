package com.sparkminds.library.book.repository;

import com.sparkminds.library.book.entity.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;

public interface BookRepository
                extends JpaRepository<Book, Long>,
                JpaSpecificationExecutor<Book> {

        Optional<Book> findByIsbnIgnoreCase(String isbn);

        boolean existsByIsbnIgnoreCase(String isbn);

        boolean existsByIsbnIgnoreCaseAndIdNot(
                        String isbn,
                        Long id);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        select book
                          from Book book
                         where book.id = :id
                        """)
        Optional<Book> findByIdForUpdate(
                        @Param("id") Long id);

        @EntityGraph(attributePaths = { "category", "authors" })
        @Query("select book from Book book where book.id = :id")
        Optional<Book> findDetailedById(
                        @Param("id") Long id);
}