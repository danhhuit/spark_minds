package com.sparkminds.library.profile.controller;

import com.sparkminds.library.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@Transactional
class ProfileControllerIntegrationTest
        extends AbstractIntegrationTest {

    private static final String MEMBER_PASSWORD =
            "Member@123";

    private String adminAccessToken;
    private String memberAccessToken;
    private long memberUserId;
    private long memberProfileId;

    @BeforeEach
    void setUp() throws Exception {
        adminAccessToken =
                loginAsAdminAndGetAccessToken();

        JsonNode member = createMember(
                "profile-member@test.local",
                "Profile Member"
        );

        memberUserId = member.get("userId").asLong();
        memberProfileId = member.get("id").asLong();
        memberAccessToken = loginAndGetAccessToken(
                "profile-member@test.local",
                MEMBER_PASSWORD
        );
    }

    @Test
    void memberCanReadOwnProfile() throws Exception {
        mockMvc.perform(get("/api/profile")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(memberAccessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(memberUserId))
                .andExpect(jsonPath("$.memberProfileId")
                        .value(memberProfileId))
                .andExpect(jsonPath("$.username")
                        .value("profile-member@test.local"))
                .andExpect(jsonPath("$.email")
                        .value("profile-member@test.local"))
                .andExpect(jsonPath("$.roles[0]")
                        .value("USER"))
                .andExpect(jsonPath("$.phone")
                        .value("0912345678"))
                .andExpect(jsonPath("$.profileComplete")
                        .value(true));
    }

    @Test
    void memberCanUpdateProfileAndUsername()
            throws Exception {
        mockMvc.perform(put("/api/profile")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(memberAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "Nguyễn.Danh_25",
                                  "fullName": "Nguyễn Thành Danh",
                                  "dateOfBirth": "2002-05-14",
                                  "phone": "0901234567",
                                  "address": "Thành phố Hồ Chí Minh"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value("Nguyễn.Danh_25"))
                .andExpect(jsonPath("$.fullName")
                        .value("Nguyễn Thành Danh"))
                .andExpect(jsonPath("$.dateOfBirth")
                        .value("2002-05-14"))
                .andExpect(jsonPath("$.phone")
                        .value("0901234567"))
                .andExpect(jsonPath("$.profileComplete")
                        .value(true));

        // The access token still contains the previous subject.
        // The uid claim must continue to identify the user.
        mockMvc.perform(get("/api/profile")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(memberAccessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(memberUserId))
                .andExpect(jsonPath("$.username")
                        .value("Nguyễn.Danh_25"));
    }

    @Test
    void incompleteProfileIsReported()
            throws Exception {
        mockMvc.perform(put("/api/profile")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(memberAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "profile.member",
                                  "fullName": "Profile Member",
                                  "dateOfBirth": null,
                                  "phone": "",
                                  "address": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone")
                        .doesNotExist())
                .andExpect(jsonPath("$.dateOfBirth")
                        .doesNotExist())
                .andExpect(jsonPath("$.profileComplete")
                        .value(false));
    }

    @Test
    void usernameMustBeValid() throws Exception {
        mockMvc.perform(put("/api/profile")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(memberAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "invalid username!",
                                  "fullName": "Profile Member",
                                  "dateOfBirth": "1995-01-01",
                                  "phone": "0912345678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username")
                        .exists());
    }

    @Test
    void usernameCannotMatchAnotherUsersLoginIdentifier()
            throws Exception {
        createMember(
                "reserved-login@test.local",
                "Reserved Login"
        );

        String reservedUserToken =
                loginAndGetAccessToken(
                        "reserved-login@test.local",
                        MEMBER_PASSWORD
                );

        mockMvc.perform(put("/api/profile")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(reservedUserToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "reserved.login",
                                  "fullName": "Reserved Login",
                                  "dateOfBirth": "1995-01-01",
                                  "phone": "0912345678"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/profile")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(memberAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "reserved.login",
                                  "fullName": "Profile Member",
                                  "dateOfBirth": "1995-01-01",
                                  "phone": "0912345678"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Username is already in use"));
    }

    @Test
    void adminWithoutMemberProfileCanReadAndUpdateUsername()
            throws Exception {
        mockMvc.perform(get("/api/profile")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value("admin"))
                .andExpect(jsonPath("$.memberProfileId")
                        .doesNotExist())
                .andExpect(jsonPath("$.profileComplete")
                        .value(false));

        mockMvc.perform(put("/api/profile")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "library.admin",
                                  "fullName": "Ignored Admin Profile",
                                  "dateOfBirth": "1990-01-01",
                                  "phone": "0911222333",
                                  "address": "Ignored"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value("library.admin"))
                .andExpect(jsonPath("$.memberProfileId")
                        .doesNotExist())
                .andExpect(jsonPath("$.fullName")
                        .doesNotExist());
    }

    @Test
    void profileEndpointsRequireAuthentication()
            throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "anonymous"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode createMember(
            String email,
            String fullName
    ) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/admin/members")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(adminAccessToken)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "email": "%s",
                                          "password": "%s",
                                          "fullName": "%s",
                                          "dateOfBirth": "1995-01-01",
                                          "phone": "0912345678",
                                          "address": "Profile Test Address"
                                        }
                                        """.formatted(
                                        email,
                                        MEMBER_PASSWORD,
                                        fullName
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password")
                        .doesNotExist())
                .andExpect(jsonPath("$.passwordConfigured")
                        .value(true))
                .andReturn();

        return objectMapper.readTree(
                result.getResponse()
                        .getContentAsString()
        );
    }
}
