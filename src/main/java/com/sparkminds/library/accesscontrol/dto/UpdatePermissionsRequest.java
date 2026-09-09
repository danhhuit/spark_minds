package com.sparkminds.library.accesscontrol.dto;

import com.sparkminds.library.member.entity.PermissionName;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record UpdatePermissionsRequest(@NotNull Set<PermissionName> permissions) {}
