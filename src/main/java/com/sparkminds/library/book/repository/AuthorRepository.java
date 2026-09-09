package com.sparkminds.library.book.repository;

import com.sparkminds.library.book.entity.Author;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {

  Optional<Author> findByNameIgnoreCase(String name);

  List<Author> findByIdIn(Collection<Long> ids);

  List<Author> findAllByOrderByNameAsc();

  boolean existsByNameIgnoreCase(String name);
}
