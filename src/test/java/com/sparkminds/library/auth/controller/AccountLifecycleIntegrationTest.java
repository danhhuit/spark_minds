package com.sparkminds.library.auth.controller;

import com.sparkminds.library.integration.AbstractIntegrationTest;
import com.sparkminds.library.mail.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
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
class AccountLifecycleIntegrationTest
        extends AbstractIntegrationTest {

    private static final String INITIAL_PASSWORD =
            "Member@123";
    private static final String NEW_PASSWORD =
            "Changed@456";

    @MockitoBean
    private MailService mailService;

    @BeforeEach
    void resetMailService() {
        reset(mailService);
    }

    @Test
    void registrationRequiresEmailVerificationBeforeLogin()
            throws Exception {
        String email = "register.lifecycle@example.com";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of(
                                        "email", email,
                                        "password",
                                        INITIAL_PASSWORD
                                )
                        )))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                email,
                                INITIAL_PASSWORD
                        )))
                .andExpect(status().isUnauthorized());

        String verificationToken =
                captureVerificationToken(email);

        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", verificationToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                email,
                                INITIAL_PASSWORD
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .isNotEmpty());
    }

    @Test
    void registrationValidatesEmailAndPassword()
            throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": "weak"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.password")
                        .exists());

        verify(mailService, never())
                .sendVerificationEmail(
                        any(String.class),
                        any(String.class)
        );
    }

    @Test
    void forgotAndResetPasswordInvalidatesOldPassword()
            throws Exception {
        String email = "reset.lifecycle@example.com";
        createMember(email);
        reset(mailService);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", email)
                        )))
                .andExpect(status().isOk());

        String resetToken =
                capturePasswordResetToken(email);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of(
                                        "token", resetToken,
                                        "newPassword",
                                        NEW_PASSWORD
                                )
                        )))
                .andExpect(status().isOk());

        expectLoginStatus(
                email,
                INITIAL_PASSWORD,
                401
        );
        expectLoginStatus(email, NEW_PASSWORD, 200);
    }

    @Test
    void forgotPasswordDoesNotRevealUnknownEmail()
            throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "unknown@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "If the email exists, "
                                + "a password reset email was sent."
                ));

        verify(mailService, never())
                .sendPasswordResetEmail(
                        any(String.class),
                        any(String.class)
        );
    }

    @Test
    void changePasswordRevokesCurrentTokens()
            throws Exception {
        String email = "change.password@example.com";
        createMember(email);
        JsonNode tokens = login(email, INITIAL_PASSWORD);
        String accessToken =
                tokens.get("accessToken").asText();
        String refreshToken =
                tokens.get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/change-password")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of(
                                        "currentPassword",
                                        INITIAL_PASSWORD,
                                        "newPassword",
                                        NEW_PASSWORD
                                )
                        )))
                .andExpect(status().isOk());

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

        expectLoginStatus(email, NEW_PASSWORD, 200);
    }

    @Test
    void emailChangeRequiresCodeAndRevokesCurrentTokens()
            throws Exception {
        String oldEmail = "old.email@example.com";
        String newEmail = "new.email@example.com";
        createMember(oldEmail);
        JsonNode tokens = login(
                oldEmail,
                INITIAL_PASSWORD
        );
        String accessToken =
                tokens.get("accessToken").asText();
        reset(mailService);

        mockMvc.perform(post(
                        "/api/auth/change-email/request")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("newEmail", newEmail)
                        )))
                .andExpect(status().isOk());

        String code = captureEmailChangeCode(
                newEmail
        );

        mockMvc.perform(post(
                        "/api/auth/change-email/verify")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("code", code)
                        )))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        ))
                .andExpect(status().isUnauthorized());

        expectLoginStatus(
                oldEmail,
                INITIAL_PASSWORD,
                401
        );
        expectLoginStatus(
                newEmail,
                INITIAL_PASSWORD,
                200
        );
    }

    @Test
    void wrongEmailChangeCodeReturnsBadRequest()
            throws Exception {
        String email = "wrong.code@example.com";
        createMember(email);
        String accessToken = login(
                email,
                INITIAL_PASSWORD
        ).get("accessToken").asText();

        mockMvc.perform(post(
                        "/api/auth/change-email/request")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newEmail":
                                    "wrong.code.new@example.com"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post(
                        "/api/auth/change-email/verify")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "000000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Verification code is incorrect"
                ));
    }

    private void createMember(String email)
            throws Exception {
        String adminToken =
                loginAsAdminAndGetAccessToken();

        mockMvc.perform(post("/api/admin/members")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of(
                                        "email", email,
                                        "password",
                                        INITIAL_PASSWORD,
                                        "fullName",
                                        "Lifecycle Test Member",
                                        "dateOfBirth",
                                        "2000-01-01",
                                        "phone", "0901234567",
                                        "address",
                                        "Test address"
                                )
                        )))
                .andExpect(status().isCreated());
    }

    private JsonNode login(
            String usernameOrEmail,
            String password
    ) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(loginJson(
                                        usernameOrEmail,
                                        password
                                )))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(
                result.getResponse().getContentAsString()
        );
    }

    private void expectLoginStatus(
            String usernameOrEmail,
            String password,
            int expectedStatus
    ) throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(
                                usernameOrEmail,
                                password
                        )))
                .andExpect(status().is(expectedStatus));
    }

    private String loginJson(
            String usernameOrEmail,
            String password
    ) throws Exception {
        return objectMapper.writeValueAsString(
                Map.of(
                        "usernameOrEmail",
                        usernameOrEmail,
                        "password",
                        password
                )
        );
    }

    private String refreshJson(String refreshToken)
            throws Exception {
        return objectMapper.writeValueAsString(
                Map.of("refreshToken", refreshToken)
        );
    }

    private String captureVerificationToken(
            String email
    ) {
        ArgumentCaptor<String> tokenCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(mailService).sendVerificationEmail(
                org.mockito.ArgumentMatchers.eq(email),
                tokenCaptor.capture()
        );

        return tokenCaptor.getValue();
    }

    private String capturePasswordResetToken(
            String email
    ) {
        ArgumentCaptor<String> tokenCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(mailService).sendPasswordResetEmail(
                org.mockito.ArgumentMatchers.eq(email),
                tokenCaptor.capture()
        );

        return tokenCaptor.getValue();
    }

    private String captureEmailChangeCode(
            String newEmail
    ) {
        ArgumentCaptor<String> codeCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(mailService).sendEmailChangeCode(
                org.mockito.ArgumentMatchers.eq(newEmail),
                codeCaptor.capture()
        );

        return codeCaptor.getValue();
    }
}
