package com.sparkminds.library.security.jwt;

import com.sparkminds.library.config.JwtProperties;
import com.sparkminds.library.config.TimeToLiveProperties;
import com.sparkminds.library.security.service.CustomUserPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

  private final JwtEncoder jwtEncoder;
  private final JwtProperties jwtProperties;
  private final TimeToLiveProperties timeToLiveProperties;

  public GeneratedAccessToken generateAccessToken(CustomUserPrincipal principal) {
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plus(timeToLiveProperties.accessToken());

    String jti = UUID.randomUUID().toString();

    List<String> authorities =
        principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    List<String> roles =
        authorities.stream().filter(authority -> authority.startsWith("ROLE_")).toList();
    List<String> permissions =
        authorities.stream().filter(authority -> !authority.startsWith("ROLE_")).toList();
    // iss: issuer của ứng dụng
    // sub: subject của token, thường là username hoặc email
    // iat: thời gian phát hành token
    // nbf: thời gian bắt đầu có hiệu lực của token
    // exp: thời gian hết hạn của token
    // jti: JWT ID, một giá trị duy nhất để xác định token
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer())
            .subject(principal.getUsername())
            .issuedAt(issuedAt)
            .notBefore(issuedAt)
            .expiresAt(expiresAt)
            .id(jti)
            .claim("uid", principal.getId())
            .claim("email", principal.getEmail())
            .claim("authv", principal.getAuthorizationVersion())
            .claim("roles", roles)
            .claim("permissions", permissions)
            .claim("authorities", authorities)
            .build();

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();

    String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

    return new GeneratedAccessToken(token, jti, issuedAt, expiresAt);
  }

  public record GeneratedAccessToken(
      String value, String jti, Instant issuedAt, Instant expiresAt) {}
}
