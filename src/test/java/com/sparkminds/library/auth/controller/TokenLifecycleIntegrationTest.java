package com.sparkminds.library.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sparkminds.library.integration.AbstractIntegrationTest;
import com.sparkminds.library.auth.entity.AccessToken;
import com.sparkminds.library.auth.repository.AccessTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import tools.jackson.databind.JsonNode;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class TokenLifecycleIntegrationTest extends AbstractIntegrationTest {

  @Autowired private AccessTokenRepository accessTokenRepository;

  @Autowired private JwtDecoder jwtDecoder;

  @Test
  void loginStoresHashedAccessTokenMetadata() throws Exception {
    JsonNode tokens = loginAsAdmin();
    String rawAccessToken = tokens.get("accessToken").asText();
    String jti = jwtDecoder.decode(rawAccessToken).getId();

    AccessToken stored =
        accessTokenRepository
            .findAll()
            .stream()
            .filter(token -> token.getJti().equals(jti))
            .findFirst()
            .orElseThrow();

    org.assertj.core.api.Assertions.assertThat(stored.getTokenHash())
        .hasSize(64)
        .doesNotContain(rawAccessToken);
    org.assertj.core.api.Assertions.assertThat(stored.getUser().getUsername()).isEqualTo("admin");
    org.assertj.core.api.Assertions.assertThat(stored.isRevoked()).isFalse();
    org.assertj.core.api.Assertions.assertThat(stored.getIssuedAt()).isNotNull();
    org.assertj.core.api.Assertions.assertThat(stored.getExpiresAt()).isAfter(stored.getIssuedAt());
  }

  @Test
  void refreshRotatesTokenAndOldRefreshTokenCannotBeReused() throws Exception {
    JsonNode loginTokens = loginAsAdmin();
    String oldRefreshToken = loginTokens.get("refreshToken").asText();

    MvcResult refreshResult =
        mockMvc
            .perform(
                post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(refreshJson(oldRefreshToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andReturn();

    JsonNode rotatedTokens =
        objectMapper.readTree(refreshResult.getResponse().getContentAsString());

    String newAccessToken = rotatedTokens.get("accessToken").asText();
    String newRefreshToken = rotatedTokens.get("refreshToken").asText();

    org.assertj.core.api.Assertions.assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);

    mockMvc
        .perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(newAccessToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("admin"));

    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshJson(oldRefreshToken)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void logoutRevokesBothAccessAndRefreshTokens() throws Exception {
    JsonNode tokens = loginAsAdmin();
    String accessToken = tokens.get("accessToken").asText();
    String refreshToken = tokens.get("refreshToken").asText();
    String jti = jwtDecoder.decode(accessToken).getId();

    mockMvc
        .perform(
            post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshJson(refreshToken)))
        .andExpect(status().isNoContent());

    AccessToken stored =
        accessTokenRepository
            .findAll()
            .stream()
            .filter(token -> token.getJti().equals(jti))
            .findFirst()
            .orElseThrow();
    org.assertj.core.api.Assertions.assertThat(stored.isRevoked()).isTrue();
    org.assertj.core.api.Assertions.assertThat(stored.getRevokedAt()).isNotNull();

    mockMvc
        .perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
        .andExpect(status().isUnauthorized());

    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshJson(refreshToken)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void invalidOrBlankRefreshTokenIsRejected() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshJson("not-a-valid-refresh-token")))
        .andExpect(status().isUnauthorized());

    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshJson("")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.refreshToken").exists());
  }

  private JsonNode loginAsAdmin() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                          "usernameOrEmail": "admin",
                                          "password": "admin"
                                        }
                                        """))
            .andExpect(status().isOk())
            .andReturn();

    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private String refreshJson(String refreshToken) throws Exception {
    return objectMapper.writeValueAsString(java.util.Map.of("refreshToken", refreshToken));
  }
}
