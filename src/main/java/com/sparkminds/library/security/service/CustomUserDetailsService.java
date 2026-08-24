package com.sparkminds.library.security.service;

import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        if (identifier == null || identifier.isBlank()) {
            throw new UsernameNotFoundException(
                    "Invalid username or email"
            );
        }

        String normalizedIdentifier =
                identifier.trim().toLowerCase(Locale.ROOT);

        UserAccount userAccount = userAccountRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(
                        normalizedIdentifier,
                        normalizedIdentifier
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Invalid username or password"
                        )
                );

        return CustomUserPrincipal.from(userAccount);
    }
}