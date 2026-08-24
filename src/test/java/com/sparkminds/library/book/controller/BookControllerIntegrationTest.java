package com.sparkminds.library.book.controller;

import com.sparkminds.library.book.entity.Author;
import com.sparkminds.library.book.entity.Book;
import com.sparkminds.library.book.entity.Category;
import com.sparkminds.library.book.repository.AuthorRepository;
import com.sparkminds.library.book.repository.BookRepository;
import com.sparkminds.library.book.repository.CategoryRepository;
import com.sparkminds.library.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class BookControllerIntegrationTest
        extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    private Long categoryId;
    private Category category;
    private String adminAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        category = categoryRepository
                .findByNameIgnoreCase("Technology")
                .orElseThrow();

        categoryId = category.getId();
        adminAccessToken =
                loginAsAdminAndGetAccessToken();
    }

    @Test
    void adminCanCreateBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBookJson(
                                "978-TEST-0001",
                                "Integration Testing"
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn")
                        .value("978-TEST-0001"))
                .andExpect(jsonPath("$.title")
                        .value("Integration Testing"))
                .andExpect(jsonPath("$.totalQuantity")
                        .value(5))
                .andExpect(jsonPath("$.availableQuantity")
                        .value(5))
                .andExpect(jsonPath("$.active")
                        .value(true))
                .andExpect(jsonPath("$.category.name")
                        .value("Technology"))
                .andExpect(jsonPath("$.authors[0].name")
                        .value("Test Author"));
    }

    @Test
    void invalidBookReturnsValidationErrors()
            throws Exception {
        mockMvc.perform(post("/api/books")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isbn": "",
                                  "title": "",
                                  "publishedDate": "2035-01-01",
                                  "totalQuantity": -1,
                                  "categoryId": null,
                                  "authorNames": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.isbn")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.title")
                        .exists())
                .andExpect(jsonPath(
                        "$.fieldErrors.totalQuantity"
                ).exists())
                .andExpect(jsonPath("$.fieldErrors.categoryId")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.authorNames")
                        .exists());
    }

    @Test
    void userCannotCreateBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBookJson(
                                "978-TEST-0002",
                                "Forbidden Book"
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanSearchBooksWithMaximumTenPerPage()
            throws Exception {
        seedBooks(12);

        mockMvc.perform(get("/api/books")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        ))
                        .param("keyword", "Pagination Book")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "title")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()")
                        .value(10))
                .andExpect(jsonPath("$.page")
                        .value(0))
                .andExpect(jsonPath("$.size")
                        .value(10))
                .andExpect(jsonPath("$.totalElements")
                        .value(12))
                .andExpect(jsonPath("$.totalPages")
                        .value(2));
    }

    @Test
    void pageSizeAboveTenReturnsBadRequest()
            throws Exception {
        mockMvc.perform(get("/api/books")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        ))
                        .param("page", "0")
                        .param("size", "11"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanUpdateBook() throws Exception {
        Long bookId = createBookThroughApi(
                "978-TEST-0003",
                "Old Title"
        );

        mockMvc.perform(put("/api/books/{id}", bookId)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isbn": "978-TEST-0003",
                                  "title": "Updated Title",
                                  "description": "Updated description",
                                  "publisher": "Updated Publisher",
                                  "publishedDate": "2021-05-20",
                                  "totalQuantity": 8,
                                  "active": true,
                                  "categoryId": %d,
                                  "authorNames": ["Updated Author"]
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Updated Title"))
                .andExpect(jsonPath("$.totalQuantity")
                        .value(8))
                .andExpect(jsonPath("$.availableQuantity")
                        .value(8))
                .andExpect(jsonPath("$.authors[0].name")
                        .value("Updated Author"));
    }

    @Test
    void deletingBookPerformsSoftDelete()
            throws Exception {
        Long bookId = createBookThroughApi(
                "978-TEST-0004",
                "Book To Delete"
        );

        mockMvc.perform(delete("/api/books/{id}", bookId)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/{id}", bookId)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active")
                        .value(false));
    }

    @Test
    void duplicateIsbnReturnsConflict()
            throws Exception {
        createBookThroughApi(
                "978-TEST-0005",
                "First Book"
        );

        mockMvc.perform(post("/api/books")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBookJson(
                                "978-TEST-0005",
                                "Duplicate ISBN"
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("ISBN has already existed"));
    }

    @Test
    void missingBookReturnsNotFound()
            throws Exception {
        mockMvc.perform(get("/api/books/99999999")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Book does not exist: 99999999"
                        ));
    }

    @Test
    void userCanLoadCategoryLookup()
            throws Exception {
        mockMvc.perform(get(
                        "/api/books/lookups/categories")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[?(@.name == 'Technology')]"
                ).exists());
    }

    @Test
    void userCanLoadAuthorLookup()
            throws Exception {
        createBookThroughApi(
                "978-TEST-LOOKUP",
                "Lookup Book"
        );

        mockMvc.perform(get(
                        "/api/books/lookups/authors")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[?(@.name == 'Test Author')]"
                ).exists());
    }

    private Long createBookThroughApi(
            String isbn,
            String title
    ) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/books")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(adminAccessToken)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(createBookJson(
                                        isbn,
                                        title
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper
                .readTree(
                        result.getResponse()
                                .getContentAsString()
                )
                .get("id")
                .asLong();
    }

    private String createBookJson(
            String isbn,
            String title
    ) {
        return """
                {
                  "isbn": "%s",
                  "title": "%s",
                  "description": "Integration test book",
                  "publisher": "Test Publisher",
                  "publishedDate": "2020-01-15",
                  "totalQuantity": 5,
                  "categoryId": %d,
                  "authorNames": ["Test Author"]
                }
                """.formatted(
                isbn,
                title,
                categoryId
        );
    }

    private void seedBooks(int count) {
        Author author = authorRepository
                .findByNameIgnoreCase("Pagination Author")
                .orElseGet(() -> {
                    Author createdAuthor = new Author();
                    createdAuthor.setName(
                            "Pagination Author"
                    );
                    return authorRepository.save(
                            createdAuthor
                    );
                });

        for (int index = 1; index <= count; index++) {
            Book book = new Book();
            book.setIsbn(
                    "PAGINATION-%03d".formatted(index)
            );
            book.setTitle(
                    "Pagination Book %03d".formatted(index)
            );
            book.setDescription("Pagination test");
            book.setPublisher("Test Publisher");
            book.setPublishedDate(
                    LocalDate.of(2020, 1, 1)
            );
            book.setTotalQuantity(1);
            book.setAvailableQuantity(1);
            book.setActive(true);
            book.setCategory(category);
            book.addAuthor(author);

            bookRepository.save(book);
        }

        bookRepository.flush();
    }
}
