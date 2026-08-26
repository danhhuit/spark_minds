package com.sparkminds.library.auth.oauth;

import com.sparkminds.library.auth.entity.OAuthIdentity;
import com.sparkminds.library.auth.repository.OAuthIdentityRepository;
import com.sparkminds.library.common.exception.BusinessException;
import com.sparkminds.library.member.entity.MemberProfile;
import com.sparkminds.library.member.entity.Role;
import com.sparkminds.library.member.entity.RoleName;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.RoleRepository;
import com.sparkminds.library.member.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleOidcUserService {

        private static final String PROVIDER = "GOOGLE";

        private final OAuthIdentityRepository identityRepository;
        private final UserAccountRepository userAccountRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        @Transactional
        // Xử lý người dùng OIDC từ Google, tạo hoặc liên kết tài khoản người dùng nếu cần
        public OidcUser loadUser(OidcUserRequest request) {
                OidcUser oidcUser = new OidcUserService().loadUser(request);

                String subject = oidcUser.getSubject();
                String email = normalizeEmail(oidcUser.getEmail());

                if (subject == null || subject.isBlank()
                                || email == null
                                || !Boolean.TRUE.equals(
                                                oidcUser.getEmailVerified())) {
                        throw new BusinessException(
                                        "Google account email is not verified");
                }

                identityRepository
                                .findByProviderAndProviderSubject(
                                                PROVIDER,
                                                subject)
                                .orElseGet(() -> createOrLinkIdentity(
                                                oidcUser,
                                                subject,
                                                email));

                return oidcUser;
        }
        //
        private OAuthIdentity createOrLinkIdentity(
                        OidcUser oidcUser,
                        String subject,
                        String email) {
                UserAccount user = userAccountRepository
                                .findByEmailIgnoreCase(email)
                                .orElseGet(() -> createGoogleUser(oidcUser, email));

                if (identityRepository
                                .existsByUser_IdAndProvider(
                                                user.getId(),
                                                PROVIDER)) {
                        throw new BusinessException(
                                        "This library account is already "
                                                        + "linked to another Google account");
                }

                user.setEmailVerified(true);
                user.setEnabled(true);

                OAuthIdentity identity = new OAuthIdentity();
                identity.setUser(user);
                identity.setProvider(PROVIDER);
                identity.setProviderSubject(subject);

                return identityRepository.save(identity);
        }
        private UserAccount createGoogleUser(
                        OidcUser oidcUser,
                        String email) {
                Role userRole = roleRepository
                                .findByName(RoleName.USER)
                                .orElseThrow(() -> new IllegalStateException(
                                                "USER role does not exist"));

                UserAccount user = new UserAccount();
                user.setUsername(resolveUsername(email));
                user.setEmail(email);
                user.setPassword(
                                passwordEncoder.encode(
                                                UUID.randomUUID() + "@Google1"));
                user.setEnabled(true);
                user.setEmailVerified(true);
                user.setAccountNonLocked(true);
                user.addRole(userRole);
                // tạo hồ sơ thành viên mới cho người dùng Google, mã thành viên được tạo tự động với tiền tố "MBR-" và 8 ký tự ngẫu nhiên
                MemberProfile profile = new MemberProfile();
                profile.setMembershipCode(
                                "MBR-"
                                                + UUID.randomUUID()
                                                                .toString()
                                                                .substring(0, 8)
                                                                .toUpperCase(Locale.ROOT));
                profile.setFullName(oidcUser.getFullName());
                user.attachMemberProfile(profile);

                return userAccountRepository.save(user);
        }

        private String resolveUsername(String email) {
                if (!userAccountRepository
                                .existsByUsernameIgnoreCase(email)) {
                        return email;
                }

                return "google_"
                                + UUID.randomUUID()
                                                .toString()
                                                .substring(0, 12);
        }

        private String normalizeEmail(String email) {
                if (email == null || email.isBlank()) {
                        return null;
                }

                return email.trim().toLowerCase(Locale.ROOT);
        }
}
