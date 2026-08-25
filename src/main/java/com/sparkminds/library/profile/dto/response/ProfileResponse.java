package com.sparkminds.library.profile.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ProfileResponse(
        Long id,
        String username,
        String email,
        List<String> roles,
        Long memberProfileId,
        String membershipCode,
        String fullName,
        LocalDate dateOfBirth,
        String phone,
        String address,
        boolean profileComplete
) {
}
