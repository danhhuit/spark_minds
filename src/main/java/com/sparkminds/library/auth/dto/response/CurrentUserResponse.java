package com.sparkminds.library.auth.dto.response;

import java.util.List;
import org.springframework.security.oauth2.jwt.Jwt;

public record CurrentUserResponse(
    Long id, String username, String email, List<String> roles, List<String> permissions) {

  public static CurrentUserResponse from(Jwt jwt) {
    Number userId = jwt.getClaim("uid");

    return new CurrentUserResponse(
        userId.longValue(),
        jwt.getSubject(),
        jwt.getClaimAsString("email"),
        jwt.getClaimAsStringList("roles"),
        jwt.getClaimAsStringList("permissions"));
  }
}
