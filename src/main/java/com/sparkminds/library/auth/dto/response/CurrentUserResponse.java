package com.sparkminds.library.auth.dto.response;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public record CurrentUserResponse(
        Long id,
        String username,
        String email,
        List<String> roles
) {

    public static CurrentUserResponse from(Jwt jwt) {
        Number userId = jwt.getClaim("uid");

        return new CurrentUserResponse(
                userId.longValue(),
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsStringList("roles")
        );
    }
}