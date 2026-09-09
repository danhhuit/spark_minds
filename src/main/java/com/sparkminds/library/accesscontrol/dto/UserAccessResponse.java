package com.sparkminds.library.accesscontrol.dto;

import com.sparkminds.library.member.entity.PermissionName;
import com.sparkminds.library.member.entity.RoleName;
import java.util.Set;

public record UserAccessResponse(
    Long userId,
    String username,
    Set<RoleName> roles,
    Set<PermissionName> directPermissions,
    Set<PermissionName> deniedPermissions,
    Set<PermissionName> effectivePermissions) {}
