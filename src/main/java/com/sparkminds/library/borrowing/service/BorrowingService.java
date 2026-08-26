package com.sparkminds.library.borrowing.service;

import com.sparkminds.library.book.entity.Book;
import com.sparkminds.library.book.repository.BookRepository;
import com.sparkminds.library.borrowing.dto.request.BorrowBookRequest;
import com.sparkminds.library.borrowing.dto.response.BorrowingResponse;
import com.sparkminds.library.borrowing.entity.Borrowing;
import com.sparkminds.library.borrowing.entity.BorrowingStatus;
import com.sparkminds.library.borrowing.mapper.BorrowingMapper;
import com.sparkminds.library.borrowing.repository.BorrowingRepository;
import com.sparkminds.library.common.api.PageResponse;
import com.sparkminds.library.common.exception.BusinessException;
import com.sparkminds.library.common.exception.ResourceNotFoundException;
import com.sparkminds.library.member.entity.MemberProfile;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.MemberProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowingService {

        private final BorrowingRepository borrowingRepository;
        private final MemberProfileRepository memberProfileRepository;
        private final BookRepository bookRepository;
        private final BorrowingMapper borrowingMapper;

        // giới hạn 14 ngày mượn sách
        @Value("${app.borrowing.default-loan-days:14}")
        private int defaultLoanDays;

        @Transactional
        public BorrowingResponse borrow(
                        Jwt jwt,
                        BorrowBookRequest request) {
                Long userId = getUserId(jwt);

                MemberProfile member = memberProfileRepository
                                .findByUserIdForUpdate(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Member profile does not exist"));

                UserAccount account = member.getUser();

                if (!account.isEnabled()) {
                        throw new BusinessException(
                                        "Member account is disabled");
                }

                if (!account.isEmailVerified()) {
                        throw new BusinessException(
                                        "Email has not been verified");
                }

                if (!account.isAccountNonLocked()) {
                        throw new BusinessException(
                                        "Member account is locked");
                }

                boolean hasActiveBorrowing = borrowingRepository
                                .existsByMember_IdAndStatus(
                                                member.getId(),
                                                BorrowingStatus.BORROWED);

                if (hasActiveBorrowing) {
                        throw new BusinessException(
                                        "Each member can borrow only "
                                                        + "one book at a time");
                }

                Book book = bookRepository
                                .findByIdForUpdate(request.bookId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Book does not exist: "
                                                                + request.bookId()));

                if (!book.isActive()) {
                        throw new BusinessException(
                                        "Book is inactive");
                }

                if (book.getAvailableQuantity() <= 0) {
                        throw new BusinessException(
                                        "Book is out of stock");
                }

                OffsetDateTime borrowedAt = OffsetDateTime.now(ZoneOffset.UTC);

                Borrowing borrowing = new Borrowing();
                borrowing.setMember(member);
                borrowing.setBook(book);
                borrowing.setStatus(
                                BorrowingStatus.BORROWED);
                borrowing.setBorrowedAt(borrowedAt);
                borrowing.setDueAt(
                                borrowedAt.plusDays(defaultLoanDays));

                book.setAvailableQuantity(
                                book.getAvailableQuantity() - 1);

                Borrowing saved = borrowingRepository.save(borrowing);

                return borrowingMapper.toResponse(saved);
        }

        @Transactional
        public BorrowingResponse returnBook(
                        Jwt jwt,
                        Long borrowingId) {
                Long userId = getUserId(jwt);

                Borrowing borrowing = borrowingRepository
                                .findByIdForUpdate(borrowingId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Borrowing does not exist: "
                                                                + borrowingId));

                boolean isAdmin = hasRole(jwt, "ROLE_ADMIN");
                boolean isOwner = borrowing
                                .getMember()
                                .getUser()
                                .getId()
                                .equals(userId);

                if (!isAdmin && !isOwner) {
                        throw new AccessDeniedException(
                                        "You cannot return this borrowing");
                }

                if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
                        throw new BusinessException(
                                        "Book has already been returned");
                }

                Book book = bookRepository
                                .findByIdForUpdate(
                                                borrowing.getBook().getId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Borrowed book does not exist"));

                if (book.getAvailableQuantity() >= book.getTotalQuantity()) {
                        throw new BusinessException(
                                        "Book inventory is inconsistent");
                }

                borrowing.setStatus(
                                BorrowingStatus.RETURNED);
                borrowing.setReturnedAt(
                                OffsetDateTime.now(ZoneOffset.UTC));

                book.setAvailableQuantity(
                                book.getAvailableQuantity() + 1);

                return borrowingMapper.toResponse(borrowing);
        }

        @Transactional(readOnly = true)
        public PageResponse<BorrowingResponse> getMyBorrowings(
                        Jwt jwt,
                        int page,
                        int size) {
                Long userId = getUserId(jwt);

                PageRequest pageable = PageRequest.of(
                                page,
                                Math.min(size, 10),
                                Sort.by(
                                                Sort.Direction.DESC,
                                                "borrowedAt"));

                Page<BorrowingResponse> result = borrowingRepository
                                .findByMember_User_Id(
                                                userId,
                                                pageable)
                                .map(borrowingMapper::toResponse);

                return PageResponse.from(result);
        }

        @Transactional(readOnly = true)
        public PageResponse<BorrowingResponse> getAllBorrowings(
                        int page,
                        int size) {
                PageRequest pageable = PageRequest.of(
                                page,
                                Math.min(size, 10),
                                Sort.by(
                                                Sort.Direction.DESC,
                                                "borrowedAt"));

                Page<BorrowingResponse> result = borrowingRepository
                                .findAllDetailed(pageable)
                                .map(borrowingMapper::toResponse);

                return PageResponse.from(result);
        }

        private Long getUserId(Jwt jwt) {
                Number userIdClaim = jwt.getClaim("uid");

                if (userIdClaim == null) {
                        throw new AccessDeniedException(
                                        "Invalid authenticated user");
                }

                return userIdClaim.longValue();
        }

        private boolean hasRole(
                        Jwt jwt,
                        String requiredRole) {
                List<String> roles = jwt.getClaimAsStringList("roles");

                return roles != null
                                && roles.contains(requiredRole);
        }
}