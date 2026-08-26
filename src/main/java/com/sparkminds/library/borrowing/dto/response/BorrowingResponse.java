package com.sparkminds.library.borrowing.dto.response;

import com.sparkminds.library.borrowing.entity.BorrowingStatus;

import java.time.OffsetDateTime;

public record BorrowingResponse(
                Long id,
                Long memberId,
                String membershipCode,
                String memberName,
                String memberEmail,
                String memberPhone,
                Long bookId,
                String isbn,
                String bookTitle,
                BorrowingStatus status,
                OffsetDateTime borrowedAt,
                OffsetDateTime dueAt,
                OffsetDateTime returnedAt,
                boolean overdue) {
}
