package com.sparkminds.library.systemconfig.dto.response;

import java.time.OffsetDateTime;

public record SystemConfigResponse(
        Long id,
        boolean maintenanceMode,
        String maintenanceMessage,
        String updatedBy,
        OffsetDateTime updatedAt) {
}