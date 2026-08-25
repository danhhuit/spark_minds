package com.sparkminds.library.profile.service;

import com.sparkminds.library.common.exception.ResourceAlreadyExistsException;
import com.sparkminds.library.common.exception.ResourceNotFoundException;
import com.sparkminds.library.member.entity.MemberProfile;
import com.sparkminds.library.member.entity.RoleName;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.repository.MemberProfileRepository;
import com.sparkminds.library.member.repository.UserAccountRepository;
import com.sparkminds.library.profile.dto.request.UpdateProfileRequest;
import com.sparkminds.library.profile.dto.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserAccountRepository userAccountRepository;
    private final MemberProfileRepository memberProfileRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getCurrentProfile(Long userId) {
        return toResponse(getUser(userId));
    }

    @Transactional
    public ProfileResponse updateCurrentProfile(
            Long userId,
            UpdateProfileRequest request
    ) {
        UserAccount user = getUser(userId);
        String username = request.username().trim();

        validateUsernameAvailable(username, userId);
        user.setUsername(username);

        MemberProfile profile = user.getMemberProfile();

        if (profile != null) {
            updateProfile(profile, request);
        } else if (!hasRole(user, RoleName.ADMIN)) {
            profile = new MemberProfile();
            profile.setMembershipCode(
                    generateMembershipCode()
            );
            updateProfile(profile, request);
            user.attachMemberProfile(profile);
        }

        userAccountRepository.saveAndFlush(user);

        return toResponse(user);
    }

    private UserAccount getUser(Long userId) {
        return userAccountRepository
                .findDetailedById(userId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User account does not exist: "
                                + userId
                    )
                );
    }

    private void validateUsernameAvailable(
            String username,
            Long userId
    ) {
        boolean collidesWithUsername =
                userAccountRepository
                    .existsByUsernameIgnoreCaseAndIdNot(
                        username,
                        userId
                    );

        boolean collidesWithEmail =
                userAccountRepository
                    .existsByEmailIgnoreCaseAndIdNot(
                        username,
                        userId
                    );

        if (collidesWithUsername
                || collidesWithEmail) {
            throw new ResourceAlreadyExistsException(
                    "Username is already in use"
            );
        }
    }

    private void updateProfile(
            MemberProfile profile,
            UpdateProfileRequest request
    ) {
        profile.setFullName(
                trimToNull(request.fullName())
        );
        profile.setDateOfBirth(
                request.dateOfBirth()
        );
        profile.setPhone(
                trimToNull(request.phone())
        );
        profile.setAddress(
                trimToNull(request.address())
        );
    }

    private ProfileResponse toResponse(UserAccount user) {
        MemberProfile profile = user.getMemberProfile();

        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles,
                profile == null ? null : profile.getId(),
                profile == null
                        ? null
                        : profile.getMembershipCode(),
                profile == null ? null : profile.getFullName(),
                profile == null
                        ? null
                        : profile.getDateOfBirth(),
                profile == null ? null : profile.getPhone(),
                profile == null ? null : profile.getAddress(),
                profile != null
                        && profile.getPhone() != null
                        && profile.getDateOfBirth() != null
        );
    }

    private boolean hasRole(
            UserAccount user,
            RoleName roleName
    ) {
        return user.getRoles()
                .stream()
                .anyMatch(role ->
                    role.getName() == roleName
                );
    }

    private String generateMembershipCode() {
        String membershipCode;

        do {
            membershipCode =
                    "MBR-"
                    + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase(Locale.ROOT);
        } while (
            memberProfileRepository
                .existsByMembershipCodeIgnoreCase(
                    membershipCode
                )
        );

        return membershipCode;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
