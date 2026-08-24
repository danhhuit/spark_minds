package com.sparkminds.library.member.dto.request;

import com.sparkminds.library.common.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateMemberRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 255)
        String email,

        @ValidPassword
        String password,

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
        String address
) {
}