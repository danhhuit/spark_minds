package com.sparkminds.library.config;

import com.sparkminds.library.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class FrontendIntegrationTest
        extends AbstractIntegrationTest {

    @Test
    void rootPageIsPublicAndContainsApplicationShell()
            throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("Spark Library")
                ))
                .andExpect(content().string(
                        containsString("id=\"app-view\"")
                ))
                .andExpect(content().string(
                        containsString(
                                "/oauth2/authorization/google"
                        )
                ));
    }

    @Test
    void googleLoginEndpointRedirectsToGoogle()
            throws Exception {
        mockMvc.perform(get(
                        "/oauth2/authorization/google"
                ))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(
                        "Location",
                        containsString(
                                "accounts.google.com"
                        )
                ));
    }

    @Test
    void frontendAssetsArePublic()
            throws Exception {
        mockMvc.perform(get("/assets/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "text/css"
                ));

        mockMvc.perform(get("/assets/css/library.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "text/css"
                ));

        mockMvc.perform(get("/assets/js/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "text/javascript"
                ))
                .andExpect(content().string(
                        containsString(
                                "renderBookDetailPage"
                        )
                ))
                .andExpect(content().string(
                        containsString(
                                "/api/saved-books"
                        )
                ))
                .andExpect(content().string(
                        containsString(
                                "item.returnedAt"
                        )
                ))
                .andExpect(content().string(
                        containsString(
                                "second: \"2-digit\""
                        )
                ))
                .andExpect(content().string(
                        containsString(
                                "const API_TEXT_VI"
                        )
                ))
                .andExpect(content().string(
                        containsString(
                                "translateTree(document.body)"
                        )
                ))
                .andExpect(content().string(
                        containsString(
                                "\"CSV file is required\""
                        )
                ))
                .andExpect(content().string(
                        containsString(
                                "\"Technology\""
                        )
                ));
    }

    @Test
    void translationCatalogContainsVietnameseAndEnglishCopies()
            throws Exception {
        byte[] javascript = mockMvc
                .perform(get("/assets/js/app.js"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        String source = new String(
                javascript,
                StandardCharsets.UTF_8
        );

        assertThat(
                source,
                containsString(
                        "\"CSV file is required\": "
                                + "\"Vui lòng chọn file CSV\""
                )
        );
        assertThat(
                source,
                containsString(
                        "\"Công nghệ\": \"Technology\""
                )
        );
    }
}
