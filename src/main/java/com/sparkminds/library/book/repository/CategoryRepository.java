package com.sparkminds.library.book.repository;

import com.sparkminds.library.book.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findByNameIgnoreCase(String name);

  List<Category> findAllByActiveTrueOrderByNameAsc();

  boolean existsByNameIgnoreCase(String name);
}
