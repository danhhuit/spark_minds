package com.sparkminds.library.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailChangeRequest(

        @NotBlank(message = "Verification code is required")
        @Pattern(
            regexp = "^\\d{6}$",
            message = "Verification code must contain 6 digits"
        )
        String code
) {
}