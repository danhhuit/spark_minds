package com.sparkminds.library.book.mapper;

import com.sparkminds.library.book.dto.response.AuthorResponse;
import com.sparkminds.library.book.dto.response.BookResponse;
import com.sparkminds.library.book.dto.response.CategoryResponse;
import com.sparkminds.library.book.entity.Book;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class BookMapper {

    public BookResponse toResponse(Book book) {
        CategoryResponse category =
                new CategoryResponse(
                        book.getCategory().getId(),
                        book.getCategory().getName(),
                        book.getCategory().getDescription()
                );

        List<AuthorResponse> authors = book
                .getAuthors()
                .stream()
                .sorted(
                    Comparator.comparing(
                        author -> author.getName()
                                .toLowerCase()
                    )
                )
                .map(author ->
                    new AuthorResponse(
                        author.getId(),
                        author.getName(),
                        author.getBiography()
                    )
                )
                .toList();

        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getDescription(),
                book.getPublisher(),
                book.getPublishedDate(),
                book.getTotalQuantity(),
                book.getAvailableQuantity(),
                book.isActive(),
                category,
                authors,
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}