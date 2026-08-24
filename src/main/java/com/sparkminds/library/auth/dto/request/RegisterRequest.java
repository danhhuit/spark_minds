package com.sparkminds.library.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 255, message = "Email is too long")
        String email,

        @NotBlank(message = "Password is required")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)"
                    + "(?=.*[@$!%*?&])"
                    + "[A-Za-z\\d@$!%*?&]{8,72}$",
            message = "Password must contain 8-72 characters, "
                    + "uppercase, lowercase, number and special character"
        )
        String password
) {
}