package com.sparkminds.library.auth.controller;

import com.sparkminds.library.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class AuthControllerIntegrationTest
        extends AbstractIntegrationTest {

    @Test
    void loginWithAdminCredentialsReturnsTokens()
            throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrEmail": "admin",
                                  "password": "admin"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.accessToken")
                        .isNotEmpty())
                .andExpect(jsonPath("$.refreshToken")
                        .isNotEmpty())
                .andExpect(jsonPath("$.expiresIn")
                        .isNumber());
    }

    @Test
    void loginWithBlankFieldsReturnsValidationErrors()
            throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrEmail": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath(
                        "$.fieldErrors.usernameOrEmail"
                ).exists())
                .andExpect(jsonPath(
                        "$.fieldErrors.password"
                ).exists());
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized()
            throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrEmail": "admin",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(
                        "Invalid username, password, or account status"
                ));
    }

    @Test
    void protectedEndpointWithoutTokenReturnsUnauthorized()
            throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void currentUserWithValidTokenReturnsAdminInformation()
            throws Exception {
        String accessToken =
                loginAsAdminAndGetAccessToken();

        mockMvc.perform(get("/api/auth/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value("admin"))
                .andExpect(jsonPath("$.email")
                        .value("admin@library.test"))
                .andExpect(jsonPath("$.roles[0]")
                        .value("ROLE_ADMIN"));
    }
}
