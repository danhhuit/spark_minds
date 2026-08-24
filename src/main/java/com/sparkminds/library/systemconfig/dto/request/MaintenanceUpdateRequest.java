package com.sparkminds.library.systemconfig.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MaintenanceUpdateRequest(

        @NotNull(message = "Maintenance status is required") Boolean enabled,

        @Size(max = 500, message = "Maintenance message must not exceed 500 characters") String message) {
}