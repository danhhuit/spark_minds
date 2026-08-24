package com.sparkminds.library.auth.dto.request;

import com.sparkminds.library.common.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(

        @NotBlank(message = "Current password is required")
        String currentPassword,

        @ValidPassword
        String newPassword
) {
}