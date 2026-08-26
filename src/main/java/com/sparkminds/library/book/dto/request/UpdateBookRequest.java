package com.sparkminds.library.book.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record UpdateBookRequest(

        @NotBlank(message = "ISBN is required") @Size(max = 20, message = "ISBN is too long") String isbn,

        @NotBlank(message = "Title is required") @Size(max = 255, message = "Title is too long") String title,

        @Size(max = 2000, message = "Description is too long") String description,

        @Size(max = 255, message = "Publisher is too long") String publisher,

        @PastOrPresent(message = "Published date cannot be in the future") LocalDate publishedDate,

        @Min(value = 0, message = "Total quantity cannot be negative") int totalQuantity,

        @NotNull(message = "Active status is required") Boolean active,

        @NotNull(message = "Category ID is required") @Positive(message = "Category ID must be positive") Long categoryId,

        @NotEmpty(message = "At least one author is required") Set<@NotBlank(message = "Author name cannot be blank") @Size(max = 150, message = "Author name is too long") String> authorNames) {
}