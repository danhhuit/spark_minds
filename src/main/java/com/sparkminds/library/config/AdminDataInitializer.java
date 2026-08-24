package com.sparkminds.library.config;

import com.sparkminds.library.member.entity.Role;
import com.sparkminds.library.member.entity.RoleName;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.RoleRepository;
import com.sparkminds.library.member.repository.UserAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

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
            @Value("${app.admin.email}") String adminEmail
    ) {
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
        if (userAccountRepository
                .existsByUsernameIgnoreCase(adminUsername)) {
            log.info("Default admin account already exists");
            return;
        }

        Role adminRole = roleRepository
                .findByName(RoleName.ADMIN)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "ADMIN role does not exist"
                        )
                );

        UserAccount admin = new UserAccount();
        admin.setUsername(
                adminUsername.toLowerCase(Locale.ROOT)
        );
        admin.setEmail(
                adminEmail.toLowerCase(Locale.ROOT)
        );
        admin.setPassword(
                passwordEncoder.encode(adminPassword)
        );
        admin.setEnabled(true);
        admin.setEmailVerified(true);
        admin.setAccountNonLocked(true);
        admin.addRole(adminRole);

        userAccountRepository.save(admin);

        log.info("Default admin account created: {}",
                adminUsername);
    }
}