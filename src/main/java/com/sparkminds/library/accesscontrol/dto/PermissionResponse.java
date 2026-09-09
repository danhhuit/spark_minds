package com.sparkminds.library.accesscontrol.dto;

import com.sparkminds.library.member.entity.Permission;
import com.sparkminds.library.member.entity.PermissionName;

public record PermissionResponse(
    PermissionName name, String resource, String action, String description) {

  public static PermissionResponse from(Permission permission) {
    return new PermissionResponse(
        permission.getName(),
        permission.getResource(),
        permission.getAction(),
        permission.getDescription());
  }
}
