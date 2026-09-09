package com.sparkminds.library.accesscontrol.dto;

import com.sparkminds.library.member.entity.PermissionName;
import com.sparkminds.library.member.entity.RoleName;
import java.util.Set;

public record RoleAccessResponse(RoleName role, Set<PermissionName> permissions) {}
