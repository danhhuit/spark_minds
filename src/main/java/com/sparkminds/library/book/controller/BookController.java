package com.sparkminds.library.book.controller;

import com.sparkminds.library.book.dto.request.BookSearchRequest;
import com.sparkminds.library.book.dto.request.CreateBookRequest;
import com.sparkminds.library.book.dto.request.UpdateBookRequest;
import com.sparkminds.library.book.dto.response.AuthorResponse;
import com.sparkminds.library.book.dto.response.BookResponse;
import com.sparkminds.library.book.dto.response.CategoryResponse;
import com.sparkminds.library.book.service.BookService;
import com.sparkminds.library.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Validated
@Tag(name = "Books")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Search books with pagination")
    public ResponseEntity<PageResponse<BookResponse>> search(
            @Valid @ModelAttribute
            BookSearchRequest request,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page cannot be negative")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be positive")
            @Max(
                value = 10,
                message = "Each page contains at most 10 records"
            )
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction
    ) {
        return ResponseEntity.ok(
                bookService.search(
                    request,
                    page,
                    size,
                    sortBy,
                    direction
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book details")
    public ResponseEntity<BookResponse> getById(
            @PathVariable
            @Positive(message = "Book ID must be positive")
            Long id
    ) {
        return ResponseEntity.ok(
                bookService.getById(id)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a book")
    public ResponseEntity<BookResponse> create(
            @Valid @RequestBody CreateBookRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a book")
    public ResponseEntity<BookResponse> update(
            @PathVariable
            @Positive(message = "Book ID must be positive")
            Long id,

            @Valid @RequestBody UpdateBookRequest request
    ) {
        return ResponseEntity.ok(
                bookService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a book")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable
            @Positive(message = "Book ID must be positive")
            Long id
    ) {
        bookService.delete(id);
    }

    @GetMapping("/lookups/categories")
    @Operation(summary = "Get categories for combobox")
    public ResponseEntity<List<CategoryResponse>>
    getCategories() {
        return ResponseEntity.ok(
                bookService.getCategories()
        );
    }

    @GetMapping("/lookups/authors")
    @Operation(summary = "Get authors for combobox")
    public ResponseEntity<List<AuthorResponse>>
    getAuthors() {
        return ResponseEntity.ok(
                bookService.getAuthors()
        );
    }
}