package com.sparkminds.library.book.dto.response;

public record AuthorResponse(
        Long id,
        String name,
        String biography
) {
}