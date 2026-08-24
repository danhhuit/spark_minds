package com.sparkminds.library.borrowing.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BorrowBookRequest(

        @NotNull(message = "Book ID is required")
        @Positive(message = "Book ID must be positive")
        Long bookId
) {
}