package com.sparkminds.library.savedbook.controller;

import com.sparkminds.library.common.api.PageResponse;
import com.sparkminds.library.savedbook.dto.response.SavedBookResponse;
import com.sparkminds.library.savedbook.dto.response.SavedBookStatusResponse;
import com.sparkminds.library.savedbook.service.SavedBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saved-books")
@RequiredArgsConstructor
@Validated
@Tag(name = "Saved books")
@SecurityRequirement(name = "bearerAuth")
public class SavedBookController {

    private final SavedBookService savedBookService;

    @GetMapping
    @Operation(summary = "Get books saved by the current user")
    public ResponseEntity<PageResponse<SavedBookResponse>> getMine(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0")
            @Min(0) int page,
            @RequestParam(defaultValue = "10")
            @Min(1) @Max(10) int size
    ) {
        return ResponseEntity.ok(
                savedBookService.getMine(
                    userId(jwt),
                    page,
                    size
                )
        );
    }

    @GetMapping("/{bookId}/status")
    @Operation(summary = "Check whether a book is saved")
    public ResponseEntity<SavedBookStatusResponse> status(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @Positive Long bookId
    ) {
        return ResponseEntity.ok(
                savedBookService.status(
                    userId(jwt),
                    bookId
                )
        );
    }

    @PostMapping("/{bookId}")
    @Operation(summary = "Save a book")
    public ResponseEntity<SavedBookResponse> save(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @Positive Long bookId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    savedBookService.save(
                        userId(jwt),
                        bookId
                    )
                );
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a book from saved books")
    public void remove(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @Positive Long bookId
    ) {
        savedBookService.remove(
                userId(jwt),
                bookId
        );
    }

    private Long userId(Jwt jwt) {
        Number claim = jwt.getClaim("uid");
        return claim.longValue();
    }
}
