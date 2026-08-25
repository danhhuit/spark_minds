package com.sparkminds.library.savedbook.dto.response;

import com.sparkminds.library.book.dto.response.BookResponse;

import java.time.OffsetDateTime;

public record SavedBookResponse(
        Long id,
        OffsetDateTime savedAt,
        BookResponse book
) {
}
