package com.sparkminds.library.security.service;

import com.sparkminds.library.member.entity.UserAccount;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

@Getter
public final class CustomUserPrincipal implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final List<GrantedAuthority> authorities;

    private CustomUserPrincipal(
            Long id,
            String username,
            String email,
            String password,
            boolean enabled,
            boolean accountNonLocked,
            List<GrantedAuthority> authorities
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.authorities = authorities;
    }

    public static CustomUserPrincipal from(
            UserAccount userAccount
    ) {
        List<GrantedAuthority> authorities =
                userAccount.getRoles()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority(
                                "ROLE_" + role.getName().name()
                        ))
                        .map(GrantedAuthority.class::cast)
                        .toList();

        return new CustomUserPrincipal(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getEmail(),
                userAccount.getPassword(),
                userAccount.isEnabled(),
                userAccount.isAccountNonLocked(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority>
    getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}