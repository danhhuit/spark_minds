package com.sparkminds.library.member.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record MemberResponse(
        Long id,
        Long userId,
        String username,
        String email,
        String membershipCode,
        String fullName,
        LocalDate dateOfBirth,
        String phone,
        String address,
        boolean enabled,
        boolean emailVerified,
        boolean accountNonLocked,
        List<String> roles,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}