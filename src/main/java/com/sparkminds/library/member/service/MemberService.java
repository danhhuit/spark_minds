package com.sparkminds.library.member.service;

import com.sparkminds.library.auth.repository.RefreshTokenRepository;
import com.sparkminds.library.common.api.PageResponse;
import com.sparkminds.library.common.exception.BusinessException;
import com.sparkminds.library.common.exception.ResourceAlreadyExistsException;
import com.sparkminds.library.common.exception.ResourceNotFoundException;
import com.sparkminds.library.member.dto.request.CreateMemberRequest;
import com.sparkminds.library.member.dto.request.MemberSearchRequest;
import com.sparkminds.library.member.dto.request.UpdateMemberRequest;
import com.sparkminds.library.member.dto.response.MemberResponse;
import com.sparkminds.library.member.entity.MemberProfile;
import com.sparkminds.library.member.entity.Role;
import com.sparkminds.library.member.entity.RoleName;
import com.sparkminds.library.member.entity.UserAccount;
import com.sparkminds.library.member.mapper.MemberMapper;
import com.sparkminds.library.member.repository.MemberProfileRepository;
import com.sparkminds.library.member.repository.RoleRepository;
import com.sparkminds.library.member.repository.UserAccountRepository;
import com.sparkminds.library.member.specification.MemberSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                "id",
                "fullName",
                "dateOfBirth",
                "membershipCode",
                "createdAt"
            );

    private final MemberProfileRepository memberProfileRepository;
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;

    @Transactional(readOnly = true)
    public PageResponse<MemberResponse> search(
            MemberSearchRequest request,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        validateDateRange(request);

        String safeSortBy =
                ALLOWED_SORT_FIELDS.contains(sortBy)
                        ? sortBy
                        : "id";

        Sort.Direction sortDirection =
                "asc".equalsIgnoreCase(direction)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(
                page,
                Math.min(size, 10),
                Sort.by(sortDirection, safeSortBy)
        );

        Page<MemberResponse> result =
                memberProfileRepository
                    .findAll(
                        MemberSpecification.from(request),
                        pageable
                    )
                    .map(memberMapper::toResponse);

        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public MemberResponse getById(Long profileId) {
        MemberProfile profile =
                getDetailedProfile(profileId);

        return memberMapper.toResponse(profile);
    }

    @Transactional
    public MemberResponse create(
            CreateMemberRequest request
    ) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userAccountRepository
                .existsByEmailIgnoreCase(email)
                || userAccountRepository
                    .existsByUsernameIgnoreCase(email)) {
            throw new ResourceAlreadyExistsException(
                    "Email has already been registered"
            );
        }

        Role userRole = roleRepository
                .findByName(RoleName.USER)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "USER role does not exist"
                    )
                );

        UserAccount user = new UserAccount();
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(
                        request.password()
                )
        );
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setAccountNonLocked(true);
        user.addRole(userRole);

        MemberProfile profile = new MemberProfile();
        profile.setMembershipCode(
                generateMembershipCode()
        );
        profile.setFullName(
                request.fullName().trim()
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

        user.attachMemberProfile(profile);

        userAccountRepository.save(user);

        return memberMapper.toResponse(profile);
    }

    @Transactional
    public MemberResponse update(
            Long profileId,
            UpdateMemberRequest request
    ) {
        MemberProfile profile =
                getDetailedProfile(profileId);

        ensureNotAdmin(profile.getUser());

        profile.setFullName(
                request.fullName().trim()
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

        profile.getUser().setEnabled(
                request.enabled()
        );

        profile.getUser().setAccountNonLocked(
                request.accountNonLocked()
        );

        if (!request.enabled()
                || !request.accountNonLocked()) {
            refreshTokenRepository
                    .revokeAllActiveTokens(
                        profile.getUser().getId(),
                        OffsetDateTime.now(
                            ZoneOffset.UTC
                        )
                    );
        }

        return memberMapper.toResponse(profile);
    }

    @Transactional
    public void deactivate(Long profileId) {
        MemberProfile profile =
                getDetailedProfile(profileId);

        UserAccount user = profile.getUser();

        ensureNotAdmin(user);

        user.setEnabled(false);

        refreshTokenRepository.revokeAllActiveTokens(
                user.getId(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    private MemberProfile getDetailedProfile(
            Long profileId
    ) {
        return memberProfileRepository
                .findDetailedById(profileId)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Member does not exist: "
                                + profileId
                    )
                );
    }

    private void ensureNotAdmin(UserAccount user) {
        boolean isAdmin = user.getRoles()
                .stream()
                .anyMatch(role ->
                    role.getName() == RoleName.ADMIN
                );

        if (isAdmin) {
            throw new BusinessException(
                    "Admin account cannot be modified "
                            + "through member management"
            );
        }
    }

    private void validateDateRange(
            MemberSearchRequest request
    ) {
        if (request.getDateOfBirthFrom() != null
                && request.getDateOfBirthTo() != null
                && request.getDateOfBirthFrom()
                    .isAfter(
                        request.getDateOfBirthTo()
                    )) {
            throw new BusinessException(
                    "Date of birth from must be "
                            + "before date of birth to"
            );
        }
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