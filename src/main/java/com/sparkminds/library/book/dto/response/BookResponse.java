package com.sparkminds.library.book.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String description,
        String publisher,
        LocalDate publishedDate,
        int totalQuantity,
        int availableQuantity,
        boolean active,
        CategoryResponse category,
        List<AuthorResponse> authors,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}