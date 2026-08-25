package com.sparkminds.library.profile.controller;

import com.sparkminds.library.profile.dto.request.UpdateProfileRequest;
import com.sparkminds.library.profile.dto.response.ProfileResponse;
import com.sparkminds.library.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ProfileResponse> getCurrentProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                profileService.getCurrentProfile(
                    userId(jwt)
                )
        );
    }

    @PutMapping
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ProfileResponse> updateCurrentProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(
                profileService.updateCurrentProfile(
                    userId(jwt),
                    request
                )
        );
    }

    private Long userId(Jwt jwt) {
        Number claim = jwt.getClaim("uid");
        return claim.longValue();
    }
}
