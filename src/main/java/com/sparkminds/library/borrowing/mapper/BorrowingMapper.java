package com.sparkminds.library.borrowing.mapper;

import com.sparkminds.library.borrowing.dto.response.BorrowingResponse;
import com.sparkminds.library.borrowing.entity.Borrowing;
import com.sparkminds.library.borrowing.entity.BorrowingStatus;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class BorrowingMapper {

    public BorrowingResponse toResponse(
            Borrowing borrowing
    ) {
        boolean overdue =
                borrowing.getStatus()
                    == BorrowingStatus.BORROWED
                && borrowing.getDueAt().isBefore(
                    OffsetDateTime.now(ZoneOffset.UTC)
                );

        return new BorrowingResponse(
                borrowing.getId(),
                borrowing.getMember().getId(),
                borrowing.getMember().getMembershipCode(),
                borrowing.getMember().getFullName(),
                borrowing.getBook().getId(),
                borrowing.getBook().getIsbn(),
                borrowing.getBook().getTitle(),
                borrowing.getStatus(),
                borrowing.getBorrowedAt(),
                borrowing.getDueAt(),
                borrowing.getReturnedAt(),
                overdue
        );
    }
}