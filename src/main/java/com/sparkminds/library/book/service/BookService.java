package com.sparkminds.library.book.service;

import com.sparkminds.library.book.dto.request.BookSearchRequest;
import com.sparkminds.library.book.dto.request.CreateBookRequest;
import com.sparkminds.library.book.dto.request.UpdateBookRequest;
import com.sparkminds.library.book.dto.response.AuthorResponse;
import com.sparkminds.library.book.dto.response.BookResponse;
import com.sparkminds.library.book.dto.response.CategoryResponse;
import com.sparkminds.library.book.entity.Author;
import com.sparkminds.library.book.entity.Book;
import com.sparkminds.library.book.entity.Category;
import com.sparkminds.library.book.mapper.BookMapper;
import com.sparkminds.library.book.repository.AuthorRepository;
import com.sparkminds.library.book.repository.BookRepository;
import com.sparkminds.library.book.repository.CategoryRepository;
import com.sparkminds.library.book.specification.BookSpecification;
import com.sparkminds.library.common.api.PageResponse;
import com.sparkminds.library.common.exception.BusinessException;
import com.sparkminds.library.common.exception.ResourceAlreadyExistsException;
import com.sparkminds.library.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                "id",
                "title",
                "isbn",
                "publisher",
                "publishedDate",
                "availableQuantity",
                "createdAt"
            );

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final BookMapper bookMapper;

    @Transactional(readOnly = true)
    public PageResponse<BookResponse> search(
            BookSearchRequest request,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        validateDateRange(request);

        String safeSortBy =
                ALLOWED_SORT_FIELDS.contains(sortBy)
                        ? sortBy
                        : "id";

        Sort.Direction sortDirection =
                "asc".equalsIgnoreCase(direction)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(
                page,
                Math.min(size, 10),
                Sort.by(sortDirection, safeSortBy)
        );

        Page<BookResponse> result = bookRepository
                .findAll(
                    BookSpecification.from(request),
                    pageable
                )
                .map(bookMapper::toResponse);

        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public BookResponse getById(Long id) {
        Book book = bookRepository
                .findDetailedById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Book does not exist: " + id
                    )
                );

        return bookMapper.toResponse(book);
    }

    @Transactional
    public BookResponse create(CreateBookRequest request) {
        String normalizedIsbn =
                normalizeIsbn(request.isbn());

        if (bookRepository
                .existsByIsbnIgnoreCase(normalizedIsbn)) {
            throw new ResourceAlreadyExistsException(
                    "ISBN has already existed"
            );
        }

        Category category =
                getActiveCategory(request.categoryId());

        Book book = new Book();
        book.setIsbn(normalizedIsbn);
        book.setTitle(request.title().trim());
        book.setDescription(
                trimToNull(request.description())
        );
        book.setPublisher(
                trimToNull(request.publisher())
        );
        book.setPublishedDate(request.publishedDate());
        book.setTotalQuantity(request.totalQuantity());
        book.setAvailableQuantity(request.totalQuantity());
        book.setActive(true);
        book.setCategory(category);

        replaceAuthors(book, request.authorNames());

        return bookMapper.toResponse(
                bookRepository.save(book)
        );
    }

    @Transactional
    public BookResponse update(
            Long id,
            UpdateBookRequest request
    ) {
        Book book = bookRepository
                .findDetailedById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Book does not exist: " + id
                    )
                );

        String normalizedIsbn =
                normalizeIsbn(request.isbn());

        if (bookRepository
                .existsByIsbnIgnoreCaseAndIdNot(
                    normalizedIsbn,
                    id
                )) {
            throw new ResourceAlreadyExistsException(
                    "ISBN has already existed"
            );
        }

        int borrowedQuantity =
                book.getTotalQuantity()
                        - book.getAvailableQuantity();

        if (request.totalQuantity() < borrowedQuantity) {
            throw new BusinessException(
                    "Total quantity cannot be smaller "
                            + "than borrowed quantity: "
                            + borrowedQuantity
            );
        }

        Category category =
                getActiveCategory(request.categoryId());

        book.setIsbn(normalizedIsbn);
        book.setTitle(request.title().trim());
        book.setDescription(
                trimToNull(request.description())
        );
        book.setPublisher(
                trimToNull(request.publisher())
        );
        book.setPublishedDate(request.publishedDate());
        book.setTotalQuantity(request.totalQuantity());
        book.setAvailableQuantity(
                request.totalQuantity() - borrowedQuantity
        );
        book.setActive(request.active());
        book.setCategory(category);

        replaceAuthors(book, request.authorNames());

        return bookMapper.toResponse(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = bookRepository
                .findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Book does not exist: " + id
                    )
                );

        if (book.getAvailableQuantity()
                < book.getTotalQuantity()) {
            throw new BusinessException(
                    "Cannot delete a book that "
                            + "is currently borrowed"
            );
        }

        // Soft delete để giữ lịch sử mượn sách.
        book.setActive(false);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository
                .findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(category ->
                    new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                    )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> getAuthors() {
        return authorRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(author ->
                    new AuthorResponse(
                        author.getId(),
                        author.getName(),
                        author.getBiography()
                    )
                )
                .toList();
    }

    private Category getActiveCategory(Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Category does not exist: "
                                + categoryId
                    )
                );

        if (!category.isActive()) {
            throw new BusinessException(
                    "Category is inactive"
            );
        }

        return category;
    }

    private void replaceAuthors(
            Book book,
            Set<String> authorNames
    ) {
        Set<Author> oldAuthors =
                new HashSet<>(book.getAuthors());

        oldAuthors.forEach(book::removeAuthor);

        authorNames.stream()
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .map(this::findOrCreateAuthor)
                .forEach(book::addAuthor);
    }

    private Author findOrCreateAuthor(String name) {
        return authorRepository
                .findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Author author = new Author();
                    author.setName(name);

                    return authorRepository.save(author);
                });
    }

    private void validateDateRange(
            BookSearchRequest request
    ) {
        if (request.getPublishedFrom() != null
                && request.getPublishedTo() != null
                && request.getPublishedFrom()
                    .isAfter(request.getPublishedTo())) {
            throw new BusinessException(
                    "Published from must be before "
                            + "published to"
            );
        }
    }

    private String normalizeIsbn(String isbn) {
        return isbn
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}