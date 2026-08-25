package com.sparkminds.library.savedbook.service;

import com.sparkminds.library.book.entity.Book;
import com.sparkminds.library.book.mapper.BookMapper;
import com.sparkminds.library.book.repository.BookRepository;
import com.sparkminds.library.common.api.PageResponse;
import com.sparkminds.library.common.exception.BusinessException;
import com.sparkminds.library.common.exception.ResourceNotFoundException;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.UserAccountRepository;
import com.sparkminds.library.savedbook.dto.response.SavedBookResponse;
import com.sparkminds.library.savedbook.dto.response.SavedBookStatusResponse;
import com.sparkminds.library.savedbook.entity.SavedBook;
import com.sparkminds.library.savedbook.repository.SavedBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavedBookService {

    private final SavedBookRepository savedBookRepository;
    private final UserAccountRepository userAccountRepository;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Transactional
    public SavedBookResponse save(
            Long userId,
            Long bookId
    ) {
        return savedBookRepository
                .findByUser_IdAndBook_Id(userId, bookId)
                .map(this::toResponse)
                .orElseGet(() ->
                    createSavedBook(userId, bookId)
                );
    }

    @Transactional
    public void remove(
            Long userId,
            Long bookId
    ) {
        savedBookRepository
                .findByUser_IdAndBook_Id(userId, bookId)
                .ifPresent(savedBookRepository::delete);
    }

    @Transactional(readOnly = true)
    public SavedBookStatusResponse status(
            Long userId,
            Long bookId
    ) {
        ensureBookExists(bookId);

        return new SavedBookStatusResponse(
                savedBookRepository
                    .existsByUser_IdAndBook_Id(
                        userId,
                        bookId
                    )
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<SavedBookResponse> getMine(
            Long userId,
            int page,
            int size
    ) {
        PageRequest pageable = PageRequest.of(
                page,
                Math.min(size, 10),
                Sort.by(
                    Sort.Direction.DESC,
                    "savedAt"
                )
        );

        Page<SavedBookResponse> result =
                savedBookRepository
                    .findByUser_Id(userId, pageable)
                    .map(this::toResponse);

        return PageResponse.from(result);
    }

    private SavedBookResponse createSavedBook(
            Long userId,
            Long bookId
    ) {
        UserAccount user = userAccountRepository
                .findById(userId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User account does not exist: "
                                + userId
                    )
                );

        Book book = bookRepository
                .findDetailedById(bookId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Book does not exist: "
                                + bookId
                    )
                );

        if (!book.isActive()) {
            throw new BusinessException(
                    "Inactive books cannot be saved"
            );
        }

        SavedBook savedBook = new SavedBook();
        savedBook.setUser(user);
        savedBook.setBook(book);

        return toResponse(
                savedBookRepository.save(savedBook)
        );
    }

    private void ensureBookExists(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException(
                    "Book does not exist: " + bookId
            );
        }
    }

    private SavedBookResponse toResponse(
            SavedBook savedBook
    ) {
        return new SavedBookResponse(
                savedBook.getId(),
                savedBook.getSavedAt(),
                bookMapper.toResponse(
                    savedBook.getBook()
                )
        );
    }
}
