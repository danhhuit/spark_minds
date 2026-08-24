package com.sparkminds.library.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeEmailRequest(

        @NotBlank(message = "New email is required")
        @Email(message = "New email format is invalid")
        @Size(max = 255, message = "Email is too long")
        String newEmail
) {
}