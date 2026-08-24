package com.sparkminds.library.book.specification;

import com.sparkminds.library.book.dto.request.BookSearchRequest;
import com.sparkminds.library.book.entity.Author;
import com.sparkminds.library.book.entity.Book;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BookSpecification {

    private BookSpecification() {
    }

    public static Specification<Book> from(
            BookSearchRequest request
    ) {
        List<Specification<Book>> specifications =
                new ArrayList<>();

        if (hasText(request.getKeyword())) {
            String value = likeValue(request.getKeyword());

            specifications.add((root, query, builder) ->
                builder.or(
                    builder.like(
                        builder.lower(root.get("title")),
                        value
                    ),
                    builder.like(
                        builder.lower(root.get("isbn")),
                        value
                    ),
                    builder.like(
                        builder.lower(root.get("publisher")),
                        value
                    )
                )
            );
        }

        if (hasText(request.getTitle())) {
            String value = likeValue(request.getTitle());

            specifications.add((root, query, builder) ->
                builder.like(
                    builder.lower(root.get("title")),
                    value
                )
            );
        }

        if (hasText(request.getIsbn())) {
            String value = request.getIsbn()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            specifications.add((root, query, builder) ->
                builder.equal(
                    builder.lower(root.get("isbn")),
                    value
                )
            );
        }

        if (hasText(request.getPublisher())) {
            String value = likeValue(request.getPublisher());

            specifications.add((root, query, builder) ->
                builder.like(
                    builder.lower(root.get("publisher")),
                    value
                )
            );
        }

        if (request.getCategoryId() != null) {
            specifications.add((root, query, builder) ->
                builder.equal(
                    root.get("category").get("id"),
                    request.getCategoryId()
                )
            );
        }

        if (hasText(request.getAuthorName())) {
            String value = likeValue(
                    request.getAuthorName()
            );

            specifications.add((root, query, builder) -> {
                Join<Book, Author> authorJoin =
                        root.join(
                            "authors",
                            JoinType.INNER
                        );

                query.distinct(true);

                return builder.like(
                    builder.lower(authorJoin.get("name")),
                    value
                );
            });
        }

        if (request.getPublishedFrom() != null) {
            specifications.add((root, query, builder) ->
                builder.greaterThanOrEqualTo(
                    root.get("publishedDate"),
                    request.getPublishedFrom()
                )
            );
        }

        if (request.getPublishedTo() != null) {
            specifications.add((root, query, builder) ->
                builder.lessThanOrEqualTo(
                    root.get("publishedDate"),
                    request.getPublishedTo()
                )
            );
        }

        if (Boolean.TRUE.equals(
                request.getAvailableOnly()
        )) {
            specifications.add((root, query, builder) ->
                builder.greaterThan(
                    root.get("availableQuantity"),
                    0
                )
            );
        }

        if (request.getActive() != null) {
            specifications.add((root, query, builder) ->
                builder.equal(
                    root.get("active"),
                    request.getActive()
                )
            );
        }

        return Specification.allOf(specifications);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String likeValue(String value) {
        return "%"
                + value.trim()
                    .toLowerCase(Locale.ROOT)
                + "%";
    }
}