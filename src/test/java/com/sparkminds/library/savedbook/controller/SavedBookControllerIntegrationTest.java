package com.sparkminds.library.savedbook.controller;

import com.sparkminds.library.book.entity.Book;
import com.sparkminds.library.book.repository.BookRepository;
import com.sparkminds.library.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class SavedBookControllerIntegrationTest
        extends AbstractIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    private String accessToken;
    private Book book;

    @BeforeEach
    void setUp() throws Exception {
        accessToken = loginAsAdminAndGetAccessToken();
        book = bookRepository
                .findByIsbnIgnoreCase("978-0132350884")
                .orElseThrow();
    }

    @Test
    void authenticatedUserCanSaveListAndRemoveBook()
            throws Exception {
        mockMvc.perform(post(
                        "/api/saved-books/{bookId}",
                        book.getId()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.book.id")
                        .value(book.getId()))
                .andExpect(jsonPath("$.book.description")
                        .isNotEmpty())
                .andExpect(jsonPath("$.savedAt")
                        .isNotEmpty());

        mockMvc.perform(get(
                        "/api/saved-books/{bookId}/status",
                        book.getId()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved")
                        .value(true));

        mockMvc.perform(get("/api/saved-books")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        )
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements")
                        .value(1))
                .andExpect(jsonPath("$.content[0].book.id")
                        .value(book.getId()));

        mockMvc.perform(delete(
                        "/api/saved-books/{bookId}",
                        book.getId()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        ))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(
                        "/api/saved-books/{bookId}/status",
                        book.getId()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved")
                        .value(false));
    }

    @Test
    void savedBookEndpointsRequireAuthentication()
            throws Exception {
        mockMvc.perform(get("/api/saved-books"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post(
                        "/api/saved-books/{bookId}",
                        book.getId()
                ))
                .andExpect(status().isUnauthorized());
    }
}
