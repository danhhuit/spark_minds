package com.sparkminds.library.accesscontrol.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class AccessControlIntegrationTest extends AbstractIntegrationTest {

  @Test
  void superAdminCanDisableAndEnableInheritedUserPermission() throws Exception {
    String adminToken = loginAsAdminAndGetAccessToken();
    String email = "permission-toggle@test.local";
    String password = "Member@123";

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/admin/members")
                    .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                        {
                                          "email": "%s",
                                          "password": "%s",
                                          "fullName": "Permission Member",
                                          "dateOfBirth": "2000-01-01",
                                          "phone": "0901234567",
                                          "address": "Ho Chi Minh City"
                                        }
                                        """
                            .formatted(email, password)))
            .andExpect(status().isCreated())
            .andReturn();

    JsonNode member = objectMapper.readTree(createResult.getResponse().getContentAsString());
    long userId = member.get("userId").asLong();
    String tokenBeforePermissionChange = loginAndGetAccessToken(email, password);

    togglePermission(adminToken, userId, false)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deniedPermissions[?(@ == 'BOOK_READ')]").exists())
        .andExpect(jsonPath("$.effectivePermissions[?(@ == 'BOOK_READ')]").doesNotExist());

    mockMvc
        .perform(
            get("/api/books")
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenBeforePermissionChange)))
        .andExpect(status().isUnauthorized());

    String deniedToken = loginAndGetAccessToken(email, password);
    mockMvc
        .perform(get("/api/books").header(HttpHeaders.AUTHORIZATION, bearer(deniedToken)))
        .andExpect(status().isForbidden());

    togglePermission(adminToken, userId, true)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.directPermissions[?(@ == 'BOOK_READ')]").exists())
        .andExpect(jsonPath("$.deniedPermissions[?(@ == 'BOOK_READ')]").doesNotExist());

    String enabledToken = loginAndGetAccessToken(email, password);
    mockMvc
        .perform(get("/api/books").header(HttpHeaders.AUTHORIZATION, bearer(enabledToken)))
        .andExpect(status().isOk());
  }

  private org.springframework.test.web.servlet.ResultActions togglePermission(
      String adminToken, long userId, boolean enabled) throws Exception {
    return mockMvc.perform(
        patch("/api/super-admin/access-control/users/" + "{userId}/permissions/BOOK_READ", userId)
            .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                                {"enabled": %s}
                                """
                    .formatted(enabled)));
  }
}
