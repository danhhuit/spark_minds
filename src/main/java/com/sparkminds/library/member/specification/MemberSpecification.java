package com.sparkminds.library.member.specification;

import com.sparkminds.library.member.dto.request.MemberSearchRequest;
import com.sparkminds.library.member.entity.MemberProfile;
import com.sparkminds.library.member.entity.Role;
import com.sparkminds.library.member.entity.UserAccount;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import com.sparkminds.library.book.entity.Book;
import com.sparkminds.library.borrowing.entity.Borrowing;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MemberSpecification {

    private MemberSpecification() {
    }

    public static Specification<MemberProfile> from(
            MemberSearchRequest request) {
        List<Specification<MemberProfile>> specifications = new ArrayList<>();
        if (request.getBookId() != null) {
            specifications.add((root, query, builder) -> {
                Join<MemberProfile, Borrowing> borrowing = root.join(
                        "borrowings",
                        JoinType.INNER);

                query.distinct(true);

                return builder.equal(
                        borrowing.get("book").get("id"),
                        request.getBookId());
            });
        }

        if (hasText(request.getBookTitle())) {
            String value = likeValue(
                    request.getBookTitle());

            specifications.add((root, query, builder) -> {
                Join<MemberProfile, Borrowing> borrowing = root.join(
                        "borrowings",
                        JoinType.INNER);

                Join<Borrowing, Book> book = borrowing.join(
                        "book",
                        JoinType.INNER);

                query.distinct(true);

                return builder.like(
                        builder.lower(book.get("title")),
                        value);
            });
        }
        if (hasText(request.getKeyword())) {
            String value = likeValue(
                    request.getKeyword());

            specifications.add((root, query, builder) -> {
                Join<MemberProfile, UserAccount> user = root.join(
                        "user",
                        JoinType.INNER);

                return builder.or(
                        builder.like(
                                builder.lower(root.get("fullName")),
                                value),
                        builder.like(
                                builder.lower(root.get("membershipCode")),
                                value),
                        builder.like(
                                builder.lower(user.get("email")),
                                value),
                        builder.like(
                                builder.lower(user.get("username")),
                                value));
            });
        }

        if (hasText(request.getFullName())) {
            String value = likeValue(
                    request.getFullName());

            specifications.add((root, query, builder) -> builder.like(
                    builder.lower(root.get("fullName")),
                    value));
        }

        if (hasText(request.getEmail())) {
            String value = likeValue(request.getEmail());

            specifications.add((root, query, builder) -> {
                Join<MemberProfile, UserAccount> user = root.join(
                        "user",
                        JoinType.INNER);

                return builder.like(
                        builder.lower(user.get("email")),
                        value);
            });
        }

        if (hasText(request.getMembershipCode())) {
            String value = likeValue(
                    request.getMembershipCode());

            specifications.add((root, query, builder) -> builder.like(
                    builder.lower(root.get("membershipCode")),
                    value));
        }

        if (request.getDateOfBirthFrom() != null) {
            specifications.add((root, query, builder) -> builder.greaterThanOrEqualTo(
                    root.get("dateOfBirth"),
                    request.getDateOfBirthFrom()));
        }

        if (request.getDateOfBirthTo() != null) {
            specifications.add((root, query, builder) -> builder.lessThanOrEqualTo(
                    root.get("dateOfBirth"),
                    request.getDateOfBirthTo()));
        }

        if (request.getEnabled() != null) {
            specifications.add((root, query, builder) -> {
                Join<MemberProfile, UserAccount> user = root.join(
                        "user",
                        JoinType.INNER);

                return builder.equal(
                        user.get("enabled"),
                        request.getEnabled());
            });
        }

        if (request.getEmailVerified() != null) {
            specifications.add((root, query, builder) -> {
                Join<MemberProfile, UserAccount> user = root.join(
                        "user",
                        JoinType.INNER);

                return builder.equal(
                        user.get("emailVerified"),
                        request.getEmailVerified());
            });
        }

        if (request.getAccountNonLocked() != null) {
            specifications.add((root, query, builder) -> {
                Join<MemberProfile, UserAccount> user = root.join(
                        "user",
                        JoinType.INNER);

                return builder.equal(
                        user.get("accountNonLocked"),
                        request.getAccountNonLocked());
            });
        }

        if (request.getRole() != null) {
            specifications.add((root, query, builder) -> {
                Join<MemberProfile, UserAccount> user = root.join(
                        "user",
                        JoinType.INNER);

                Join<UserAccount, Role> roles = user.join(
                        "roles",
                        JoinType.INNER);

                query.distinct(true);

                return builder.equal(
                        roles.get("name"),
                        request.getRole());
            });
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