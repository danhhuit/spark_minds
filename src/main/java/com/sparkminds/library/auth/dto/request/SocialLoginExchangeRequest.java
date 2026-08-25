package com.sparkminds.library.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SocialLoginExchangeRequest(

        @NotBlank(message = "Social login code is required")
        @Size(max = 100, message = "Social login code is invalid")
        String code
) {
}
