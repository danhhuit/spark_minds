package com.sparkminds.library.book.controller;

import com.sparkminds.library.book.dto.response.BookImportResponse;
import com.sparkminds.library.book.service.BookCsvImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books")
@SecurityRequirement(name = "bearerAuth")
public class BookImportController {

    private final BookCsvImportService bookCsvImportService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Import books from CSV")
    public ResponseEntity<BookImportResponse> importBooks(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(
                bookCsvImportService.importBooks(file));
    }
}