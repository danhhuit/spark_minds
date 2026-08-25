package com.sparkminds.library.config;

import com.sparkminds.library.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
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
                ));
    }
}
