package com.sparkminds.library.config;

import com.sparkminds.library.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class DocumentationIntegrationTest
        extends AbstractIntegrationTest {

    @Test
    void openApiDocumentContainsApplicationMetadataAndMainApis()
            throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value(
                        "Library Management API"
                ))
                .andExpect(jsonPath("$.info.version")
                        .value("1.0.0"))
                .andExpect(jsonPath(
                        "$.components.securitySchemes"
                                + ".bearerAuth"
                ).exists())
                .andExpect(jsonPath(
                        "$.paths['/api/auth/login']"
                ).exists())
                .andExpect(jsonPath(
                        "$.paths['/api/books']"
                ).exists())
                .andExpect(jsonPath(
                        "$.paths['/api/admin/members']"
                ).exists())
                .andExpect(jsonPath(
                        "$.paths['/api/borrowings']"
                ).exists());
    }
}
