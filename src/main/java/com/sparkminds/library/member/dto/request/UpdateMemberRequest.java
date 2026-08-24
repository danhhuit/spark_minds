package com.sparkminds.library.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateMemberRequest(

        @NotBlank(message = "Full name is required")
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
        String address,

        @NotNull(message = "Enabled is required")
        Boolean enabled,

        @NotNull(message = "Account lock status is required")
        Boolean accountNonLocked
) {
}