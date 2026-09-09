package com.sparkminds.library.config;

import com.sparkminds.library.member.entity.Role;
import com.sparkminds.library.member.entity.RoleName;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.RoleRepository;
import com.sparkminds.library.member.repository.UserAccountRepository;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class AdminDataInitializer implements ApplicationRunner {

  private final UserAccountRepository userAccountRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final String adminUsername;
  private final String adminPassword;
  private final String adminEmail;

  public AdminDataInitializer(
      UserAccountRepository userAccountRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      @Value("${app.admin.username}") String adminUsername,
      @Value("${app.admin.password}") String adminPassword,
      @Value("${app.admin.email}") String adminEmail) {
    this.userAccountRepository = userAccountRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.adminUsername = adminUsername;
    this.adminPassword = adminPassword;
    this.adminEmail = adminEmail;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    Role adminRole = requiredRole(RoleName.ADMIN);
    Role superAdminRole = requiredRole(RoleName.SUPER_ADMIN);

    var existingAdmin = userAccountRepository.findByUsernameIgnoreCase(adminUsername);

    if (existingAdmin.isPresent()) {
      UserAccount admin = existingAdmin.get();
      admin.addRole(adminRole);
      admin.addRole(superAdminRole);
      userAccountRepository.save(admin);
      log.info("Default admin account has ADMIN and SUPER_ADMIN roles");
      return;
    }

    UserAccount admin = new UserAccount();
    admin.setUsername(adminUsername.toLowerCase(Locale.ROOT));
    admin.setEmail(adminEmail.toLowerCase(Locale.ROOT));
    admin.setPassword(passwordEncoder.encode(adminPassword));
    admin.setEnabled(true);
    admin.setEmailVerified(true);
    admin.setAccountNonLocked(true);
    admin.addRole(adminRole);
    admin.addRole(superAdminRole);

    userAccountRepository.save(admin);

    log.info("Default admin account created: {}", adminUsername);
  }

  private Role requiredRole(RoleName roleName) {
    return roleRepository
        .findByName(roleName)
        .orElseThrow(() -> new IllegalStateException(roleName + " role does not exist"));
  }
}
