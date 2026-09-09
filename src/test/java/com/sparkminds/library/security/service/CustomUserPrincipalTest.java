package com.sparkminds.library.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sparkminds.library.member.entity.Permission;
import com.sparkminds.library.member.entity.PermissionName;
import com.sparkminds.library.member.entity.Role;
import com.sparkminds.library.member.entity.RoleName;
import com.sparkminds.library.member.entity.UserAccount;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class CustomUserPrincipalTest {

  @Test
  void combinesRoleAndDirectPermissionsWithoutDuplicates() {
    Permission readBook = permission(PermissionName.BOOK_READ);
    Permission importBook = permission(PermissionName.BOOK_IMPORT);

    Role role = new Role();
    role.setName(RoleName.USER);
    role.getPermissions().add(readBook);

    UserAccount user = new UserAccount();
    user.setId(10L);
    user.setUsername("member");
    user.setEmail("member@example.com");
    user.setPassword("hash");
    user.setEnabled(true);
    user.setAccountNonLocked(true);
    user.addRole(role);
    user.addDirectPermission(readBook);
    user.addDirectPermission(importBook);

    Set<String> authorities =
        CustomUserPrincipal.from(user).getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    assertEquals(Set.of("ROLE_USER", "BOOK_READ", "BOOK_IMPORT"), authorities);
  }

  private Permission permission(PermissionName name) {
    Permission permission = new Permission();
    permission.setName(name);
    permission.setResource("BOOK");
    permission.setAction("READ");
    permission.setDescription(name.name());
    return permission;
  }
}
