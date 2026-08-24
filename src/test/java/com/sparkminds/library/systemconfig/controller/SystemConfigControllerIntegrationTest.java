package com.sparkminds.library.systemconfig.controller;

import com.sparkminds.library.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@Transactional
class SystemConfigControllerIntegrationTest
        extends AbstractIntegrationTest {

    private String adminAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        adminAccessToken =
                loginAsAdminAndGetAccessToken();
    }

    @Test
    void adminCanReadAndUpdateMaintenanceConfiguration()
            throws Exception {
        mockMvc.perform(get("/api/admin/system-config")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maintenanceMode")
                        .value(false));

        enableMaintenance("The library is upgrading");

        mockMvc.perform(get("/api/admin/system-config")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maintenanceMode")
                        .value(true))
                .andExpect(jsonPath("$.maintenanceMessage")
                        .value("The library is upgrading"))
                .andExpect(jsonPath("$.updatedBy")
                        .value("admin"));
    }

    @Test
    void maintenanceModeBlocksBusinessApisButStillAllowsLogin()
            throws Exception {
        enableMaintenance("Please try again later");

        mockMvc.perform(get("/api/books")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value("Please try again later"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrEmail": "admin",
                                  "password": "admin"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .isNotEmpty());

        mockMvc.perform(put(
                        "/api/admin/system-config/maintenance")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": false,
                                  "message": ""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maintenanceMode")
                        .value(false));
    }

    @Test
    void userCannotManageSystemConfiguration()
            throws Exception {
        mockMvc.perform(get("/api/admin/system-config")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    void maintenanceUpdateRequiresEnabledFlag()
            throws Exception {
        mockMvc.perform(put(
                        "/api/admin/system-config/maintenance")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Missing enabled"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.enabled")
                        .exists());
    }

    private void enableMaintenance(String message)
            throws Exception {
        mockMvc.perform(put(
                        "/api/admin/system-config/maintenance")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": true,
                                  "message": "%s"
                                }
                                """.formatted(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maintenanceMode")
                        .value(true));
    }
}
