package com.sparkminds.library.accesscontrol.dto;

import jakarta.validation.constraints.NotNull;

public record TogglePermissionRequest(@NotNull Boolean enabled) {}
