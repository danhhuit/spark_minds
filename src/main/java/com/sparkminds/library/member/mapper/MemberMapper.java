package com.sparkminds.library.member.mapper;

import com.sparkminds.library.member.dto.response.MemberResponse;
import com.sparkminds.library.member.entity.MemberProfile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberMapper {

    public MemberResponse toResponse(
            MemberProfile profile
    ) {
        List<String> roles = profile
                .getUser()
                .getRoles()
                .stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();

        return new MemberResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getUsername(),
                profile.getUser().getEmail(),
                profile.getMembershipCode(),
                profile.getFullName(),
                profile.getDateOfBirth(),
                profile.getPhone(),
                profile.getAddress(),
                profile.getUser().isEnabled(),
                profile.getUser().isEmailVerified(),
                profile.getUser().isAccountNonLocked(),
                profile.getUser().getPassword() != null
                        && !profile.getUser()
                            .getPassword()
                            .isBlank(),
                roles,
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
