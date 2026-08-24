package com.sparkminds.library.borrowing.controller;

import com.sparkminds.library.borrowing.dto.response.BorrowingResponse;
import com.sparkminds.library.borrowing.service.BorrowingService;
import com.sparkminds.library.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/borrowings")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Borrowing Management")
@SecurityRequirement(name = "bearerAuth")
public class AdminBorrowingController {

    private final BorrowingService borrowingService;

    @GetMapping
    @Operation(summary = "Get all borrowings")
    public ResponseEntity<PageResponse<BorrowingResponse>>
    getAllBorrowings(
            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(10)
            int size
    ) {
        return ResponseEntity.ok(
                borrowingService.getAllBorrowings(
                    page,
                    size
                )
        );
    }
}