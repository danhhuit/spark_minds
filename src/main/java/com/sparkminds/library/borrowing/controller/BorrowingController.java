package com.sparkminds.library.borrowing.controller;

import com.sparkminds.library.borrowing.dto.request.BorrowBookRequest;
import com.sparkminds.library.borrowing.dto.response.BorrowingResponse;
import com.sparkminds.library.borrowing.service.BorrowingService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
@Validated
@Tag(name = "Borrowing")
@SecurityRequirement(name = "bearerAuth")
public class BorrowingController {

    private final BorrowingService borrowingService;

    @PostMapping
    @Operation(summary = "Borrow a book")
    public ResponseEntity<BorrowingResponse> borrow(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BorrowBookRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        borrowingService.borrow(jwt, request));
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Return a borrowed book")
    public ResponseEntity<BorrowingResponse> returnBook(
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable @Positive Long id) {
        return ResponseEntity.ok(
                borrowingService.returnBook(jwt, id));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my borrowing history")
    public ResponseEntity<PageResponse<BorrowingResponse>> getMyBorrowings(
            @AuthenticationPrincipal Jwt jwt,

            @RequestParam(defaultValue = "0") @Min(0) int page,

            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int size) {
        return ResponseEntity.ok(
                borrowingService.getMyBorrowings(
                        jwt,
                        page,
                        size));
    }
}