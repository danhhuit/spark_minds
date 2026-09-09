package com.sparkminds.library.accesscontrol.service;

import com.sparkminds.library.accesscontrol.dto.PermissionResponse;
import com.sparkminds.library.accesscontrol.dto.RoleAccessResponse;
import com.sparkminds.library.accesscontrol.dto.TogglePermissionRequest;
import com.sparkminds.library.accesscontrol.dto.UpdatePermissionsRequest;
import com.sparkminds.library.accesscontrol.dto.UpdateRolesRequest;
import com.sparkminds.library.accesscontrol.dto.UserAccessResponse;
import com.sparkminds.library.common.exception.BusinessException;
import com.sparkminds.library.common.exception.ResourceNotFoundException;
import com.sparkminds.library.member.entity.Permission;
import com.sparkminds.library.member.entity.PermissionName;
import com.sparkminds.library.member.entity.Role;
import com.sparkminds.library.member.entity.RoleName;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.PermissionRepository;
import com.sparkminds.library.member.repository.RoleRepository;
import com.sparkminds.library.member.repository.UserAccountRepository;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessControlService {

  private final PermissionRepository permissionRepository;
  private final RoleRepository roleRepository;
  private final UserAccountRepository userAccountRepository;

  @Transactional(readOnly = true)
  public List<PermissionResponse> getPermissions() {
    return permissionRepository.findAll(Sort.by("resource", "action")).stream()
        .map(PermissionResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<RoleAccessResponse> getRoles() {
    return roleRepository.findAllByOrderByNameAsc().stream().map(this::toRoleResponse).toList();
  }
  // Hàm này dùng để thay thế quyền của vai trò.
  @Transactional
  public RoleAccessResponse replaceRolePermissions(
      RoleName roleName, UpdatePermissionsRequest request) {
    Role role = requiredRole(roleName);
    if (roleName == RoleName.SUPER_ADMIN
        && !request.permissions().contains(PermissionName.ACCESS_CONTROL_MANAGE)) {
      throw new BusinessException("SUPER_ADMIN must keep ACCESS_CONTROL_MANAGE");
    }
    if (roleName != RoleName.SUPER_ADMIN
        && request.permissions().contains(PermissionName.ACCESS_CONTROL_MANAGE)) {
      throw new BusinessException("ACCESS_CONTROL_MANAGE belongs only to SUPER_ADMIN");
    }
    role.getPermissions().clear();
    role.getPermissions().addAll(requiredPermissions(request.permissions()));
    userAccountRepository.incrementAuthorizationVersionForRole(role);
    return toRoleResponse(role);
  }

  @Transactional(readOnly = true)
  public UserAccessResponse getUserAccess(Long userId) {
    return toUserResponse(requiredUser(userId));
  }
  // Hàm này dùng để thay thế quyền của người dùng.
  @Transactional
  public UserAccessResponse replaceUserPermissions(Long userId, UpdatePermissionsRequest request) {
    UserAccount user = requiredUser(userId);
    if (!hasRole(user, RoleName.SUPER_ADMIN)
        && request.permissions().contains(PermissionName.ACCESS_CONTROL_MANAGE)) {
      throw new BusinessException("ACCESS_CONTROL_MANAGE belongs only to SUPER_ADMIN");
    }
    user.getDirectPermissions().clear();
    user.getDirectPermissions().addAll(requiredPermissions(request.permissions()));
    incrementAuthorizationVersion(user);
    return toUserResponse(user);
  }
  // Hàm này dùng để bật hoặc tắt quyền cho người dùng.
  @Transactional
  public UserAccessResponse toggleUserPermission(
      Long userId, PermissionName permissionName, TogglePermissionRequest request) {
    UserAccount user = requiredUser(userId);
    Permission permission =
        permissionRepository
            .findByName(permissionName)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Permission does not exist: " + permissionName));

    if (permissionName == PermissionName.ACCESS_CONTROL_MANAGE) {
      if (!hasRole(user, RoleName.SUPER_ADMIN)) {
        throw new BusinessException("ACCESS_CONTROL_MANAGE belongs only to SUPER_ADMIN");
      }
      if (!request.enabled()) {
        throw new BusinessException(
            "ACCESS_CONTROL_MANAGE cannot be disabled " + "for a SUPER_ADMIN");
      }
    }

    if (request.enabled()) {
      user.removeDeniedPermission(permission);
      user.addDirectPermission(permission);
    } else {
      user.removeDirectPermission(permission);
      user.denyPermission(permission);
    }

    incrementAuthorizationVersion(user);
    return toUserResponse(user);
  }
  // Hàm này dùng để thay đổi vai trò của người dùng.
  @Transactional
  public UserAccessResponse replaceUserRoles(Long userId, UpdateRolesRequest request) {
    UserAccount user = requiredUser(userId);
    boolean currentlySuperAdmin =
        user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.SUPER_ADMIN);
    boolean remainsSuperAdmin = request.roles().contains(RoleName.SUPER_ADMIN);

    if (currentlySuperAdmin
        && !remainsSuperAdmin
        && userAccountRepository.countDistinctByRoles_Name(RoleName.SUPER_ADMIN) <= 1) {
      throw new BusinessException("The last SUPER_ADMIN cannot be demoted");
    }

    Set<Role> roles =
        request.roles().stream()
            .map(this::requiredRole)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    user.getRoles().clear();
    user.getRoles().addAll(roles);
    incrementAuthorizationVersion(user);
    return toUserResponse(user);
  }

  private UserAccount requiredUser(Long userId) {
    return userAccountRepository
        .findDetailedById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User does not exist: " + userId));
  }

  private Role requiredRole(RoleName roleName) {
    return roleRepository
        .findByName(roleName)
        .orElseThrow(() -> new ResourceNotFoundException("Role does not exist: " + roleName));
  }

  private Collection<Permission> requiredPermissions(Set<PermissionName> permissionNames) {
    if (permissionNames.isEmpty()) {
      return List.of();
    }

    List<Permission> permissions = permissionRepository.findAllByNameIn(permissionNames);

    if (permissions.size() != permissionNames.size()) {
      Set<PermissionName> found =
          permissions.stream().map(Permission::getName).collect(Collectors.toSet());
      Set<PermissionName> missing = EnumSet.copyOf(permissionNames);
      missing.removeAll(found);
      throw new ResourceNotFoundException("Permissions do not exist: " + missing);
    }

    return permissions;
  }

  private RoleAccessResponse toRoleResponse(Role role) {
    Set<PermissionName> permissions =
        role.getPermissions().stream()
            .map(Permission::getName)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    return new RoleAccessResponse(role.getName(), permissions);
  }

  private UserAccessResponse toUserResponse(UserAccount user) {
    Set<RoleName> roles =
        user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    Set<PermissionName> direct =
        user.getDirectPermissions().stream()
            .map(Permission::getName)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    Set<PermissionName> denied =
        user.getDeniedPermissions().stream()
            .map(Permission::getName)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    Set<PermissionName> effective =
        user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getName)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    effective.addAll(direct);
    effective.removeAll(denied);

    return new UserAccessResponse(
        user.getId(), user.getUsername(), roles, direct, denied, effective);
  }

  private boolean hasRole(UserAccount user, RoleName roleName) {
    return user.getRoles().stream().anyMatch(role -> role.getName() == roleName);
  }

  private void incrementAuthorizationVersion(UserAccount user) {
    user.setAuthorizationVersion(user.getAuthorizationVersion() + 1);
  }
}
