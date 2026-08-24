package com.sparkminds.library.auth.controller;

import com.sparkminds.library.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@Transactional
class TokenLifecycleIntegrationTest
        extends AbstractIntegrationTest {

    @Test
    void refreshRotatesTokenAndOldRefreshTokenCannotBeReused()
            throws Exception {
        JsonNode loginTokens = loginAsAdmin();
        String oldRefreshToken =
                loginTokens.get("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(refreshJson(
                                        oldRefreshToken
                                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .isNotEmpty())
                .andExpect(jsonPath("$.refreshToken")
                        .isNotEmpty())
                .andReturn();

        JsonNode rotatedTokens = objectMapper.readTree(
                refreshResult.getResponse()
                        .getContentAsString()
        );

        String newAccessToken =
                rotatedTokens.get("accessToken").asText();
        String newRefreshToken =
                rotatedTokens.get("refreshToken").asText();

        org.assertj.core.api.Assertions.assertThat(
                newRefreshToken
        ).isNotEqualTo(oldRefreshToken);

        mockMvc.perform(get("/api/auth/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(newAccessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value("admin"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson(
                                oldRefreshToken
                        )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesBothAccessAndRefreshTokens()
            throws Exception {
        JsonNode tokens = loginAsAdmin();
        String accessToken =
                tokens.get("accessToken").asText();
        String refreshToken =
                tokens.get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson(refreshToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        ))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson(refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidOrBlankRefreshTokenIsRejected()
            throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson(
                                "not-a-valid-refresh-token"
                        )))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.fieldErrors.refreshToken"
                ).exists());
    }

    private JsonNode loginAsAdmin() throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "usernameOrEmail": "admin",
                                          "password": "admin"
                                        }
                                        """))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(
                result.getResponse().getContentAsString()
        );
    }

    private String refreshJson(String refreshToken)
            throws Exception {
        return objectMapper.writeValueAsString(
                java.util.Map.of(
                        "refreshToken",
                        refreshToken
                )
        );
    }
}
