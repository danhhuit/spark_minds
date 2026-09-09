package com.sparkminds.library.accesscontrol.controller;

import com.sparkminds.library.accesscontrol.dto.PermissionResponse;
import com.sparkminds.library.accesscontrol.dto.RoleAccessResponse;
import com.sparkminds.library.accesscontrol.dto.TogglePermissionRequest;
import com.sparkminds.library.accesscontrol.dto.UpdatePermissionsRequest;
import com.sparkminds.library.accesscontrol.dto.UpdateRolesRequest;
import com.sparkminds.library.accesscontrol.dto.UserAccessResponse;
import com.sparkminds.library.accesscontrol.service.AccessControlService;
import com.sparkminds.library.member.entity.RoleName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/super-admin/access-control")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('ACCESS_CONTROL_MANAGE')")
@Tag(name = "Super Admin Access Control")
@SecurityRequirement(name = "bearerAuth")
public class AccessControlController {

  private final AccessControlService accessControlService;

  @GetMapping("/permissions")
  @Operation(summary = "List all available permissions")
  public ResponseEntity<List<PermissionResponse>> getPermissions() {
    return ResponseEntity.ok(accessControlService.getPermissions());
  }

  @GetMapping("/roles")
  @Operation(summary = "List roles and their permissions")
  public ResponseEntity<List<RoleAccessResponse>> getRoles() {
    return ResponseEntity.ok(accessControlService.getRoles());
  }

  @PutMapping("/roles/{roleName}/permissions")
  @Operation(summary = "Replace all permissions assigned to a role")
  public ResponseEntity<RoleAccessResponse> replaceRolePermissions(
      @PathVariable RoleName roleName, @Valid @RequestBody UpdatePermissionsRequest request) {
    return ResponseEntity.ok(accessControlService.replaceRolePermissions(roleName, request));
  }

  @GetMapping("/users/{userId}")
  @Operation(summary = "Get roles and permissions of a user")
  public ResponseEntity<UserAccessResponse> getUserAccess(@PathVariable @Positive Long userId) {
    return ResponseEntity.ok(accessControlService.getUserAccess(userId));
  }

  @PutMapping("/users/{userId}/permissions")
  @Operation(summary = "Replace direct permissions assigned to a user")
  public ResponseEntity<UserAccessResponse> replaceUserPermissions(
      @PathVariable @Positive Long userId, @Valid @RequestBody UpdatePermissionsRequest request) {
    return ResponseEntity.ok(accessControlService.replaceUserPermissions(userId, request));
  }

  @PatchMapping("/users/{userId}/permissions/{permissionName}")
  @Operation(summary = "Enable or disable one permission for a user")
  public ResponseEntity<UserAccessResponse> toggleUserPermission(
      @PathVariable @Positive Long userId,
      @PathVariable com.sparkminds.library.member.entity.PermissionName permissionName,
      @Valid @RequestBody TogglePermissionRequest request) {
    return ResponseEntity.ok(
        accessControlService.toggleUserPermission(userId, permissionName, request));
  }

  @PutMapping("/users/{userId}/roles")
  @Operation(summary = "Replace roles assigned to a user")
  public ResponseEntity<UserAccessResponse> replaceUserRoles(
      @PathVariable @Positive Long userId, @Valid @RequestBody UpdateRolesRequest request) {
    return ResponseEntity.ok(accessControlService.replaceUserRoles(userId, request));
  }
}
