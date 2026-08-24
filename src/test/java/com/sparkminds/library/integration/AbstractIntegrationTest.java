package com.sparkminds.library.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.TimeZone;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES;

    static {
        TimeZone.setDefault(
                TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        );

        POSTGRES = new PostgreSQLContainer<>(
                DockerImageName.parse("postgres:17")
        )
                .withDatabaseName("library_test")
                .withUsername("library_test")
                .withPassword("library_test");

        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );
        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String loginAsAdminAndGetAccessToken()
            throws Exception {
        return loginAndGetAccessToken(
                "admin",
                "admin"
        );
    }

    protected String loginAndGetAccessToken(
            String usernameOrEmail,
            String password
    ) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "usernameOrEmail",
                                                        usernameOrEmail,
                                                        "password",
                                                        password
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper
                .readTree(
                        result.getResponse()
                                .getContentAsString()
                )
                .get("accessToken")
                .asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
