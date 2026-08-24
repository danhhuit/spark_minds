package com.sparkminds.library.auth.dto.request;

import com.sparkminds.library.common.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(

        @NotBlank(message = "Reset token is required")
        String token,

        @ValidPassword
        String newPassword
) {
}