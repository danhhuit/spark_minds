package com.sparkminds.library.auth.controller;

import com.sparkminds.library.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class TokenInspectionIntegrationTest extends AbstractIntegrationTest {

  @Test
  void superAdminCanInspectAccessAndRefreshTokenMetadata() throws Exception {
    String accessToken = loginAsAdminAndGetAccessToken();

    mockMvc
        .perform(
            get("/api/super-admin/tokens/access")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].tokenType").value("ACCESS"))
        .andExpect(jsonPath("$.content[0].jti").isNotEmpty())
        .andExpect(jsonPath("$.content[0].fingerprint").isNotEmpty())
        .andExpect(jsonPath("$.content[0].username").value("admin"))
        .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));

    mockMvc
        .perform(
            get("/api/super-admin/tokens/refresh")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].tokenType").value("REFRESH"))
        .andExpect(jsonPath("$.content[0].fingerprint").isNotEmpty())
        .andExpect(jsonPath("$.content[0].username").value("admin"))
        .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
  }
}
