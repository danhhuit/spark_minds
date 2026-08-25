package com.sparkminds.library.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(

        @NotBlank(message = "Username is required")
        @Size(
            min = 3,
            max = 50,
            message = "Username must contain between 3 and 50 characters"
        )
        @Pattern(
            regexp = "^[\\p{L}\\p{N}._-]+$",
            message = "Username may contain only letters, numbers, dots, underscores and hyphens"
        )
        String username,

        @Size(max = 150)
        String fullName,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Pattern(
            regexp = "^$|^[0-9+() .-]{8,20}$",
            message = "Phone format is invalid"
        )
        String phone,

        @Size(max = 500)
        String address
) {
}
