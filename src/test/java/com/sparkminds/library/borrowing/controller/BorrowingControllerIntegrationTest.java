package com.sparkminds.library.borrowing.controller;

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
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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
class BorrowingControllerIntegrationTest
        extends AbstractIntegrationTest {

    private static final String MEMBER_PASSWORD =
            "Member@123";

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    private String adminAccessToken;
    private MemberFixture member;
    private Book book;

    @BeforeEach
    void setUp() throws Exception {
        adminAccessToken =
                loginAsAdminAndGetAccessToken();

        member = createMember(
                "borrower@test.local",
                "Primary Borrower"
        );

        book = createBook(
                "BORROW-BOOK-001",
                "Primary Borrowing Book",
                2,
                2,
                true
        );
    }

    @Test
    void borrowingBookDecreasesAvailableQuantity()
            throws Exception {
        mockMvc.perform(post("/api/borrowings")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(member.accessToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d
                                }
                                """.formatted(book.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId")
                        .value(member.profileId()))
                .andExpect(jsonPath("$.bookId")
                        .value(book.getId()))
                .andExpect(jsonPath("$.status")
                        .value("BORROWED"))
                .andExpect(jsonPath("$.borrowedAt")
                        .isNotEmpty())
                .andExpect(jsonPath("$.dueAt")
                        .isNotEmpty())
                .andExpect(jsonPath("$.returnedAt")
                        .doesNotExist());

        Book updatedBook = bookRepository
                .findById(book.getId())
                .orElseThrow();

        org.assertj.core.api.Assertions
                .assertThat(
                        updatedBook.getAvailableQuantity()
                )
                .isEqualTo(1);
    }

    @Test
    void returningBookRestoresAvailableQuantity()
            throws Exception {
        JsonNode borrowing = borrow(
                member.accessToken(),
                book.getId()
        );

        long borrowingId =
                borrowing.get("id").asLong();

        mockMvc.perform(post(
                        "/api/borrowings/{id}/return",
                        borrowingId
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(member.accessToken())
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("RETURNED"))
                .andExpect(jsonPath("$.returnedAt")
                        .isNotEmpty());

        Book returnedBook = bookRepository
                .findById(book.getId())
                .orElseThrow();

        org.assertj.core.api.Assertions
                .assertThat(
                        returnedBook.getAvailableQuantity()
                )
                .isEqualTo(2);
    }

    @Test
    void memberCannotHaveTwoActiveBorrowings()
            throws Exception {
        borrow(member.accessToken(), book.getId());

        Book secondBook = createBook(
                "BORROW-BOOK-002",
                "Second Borrowing Book",
                1,
                1,
                true
        );

        mockMvc.perform(post("/api/borrowings")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(member.accessToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d
                                }
                                """.formatted(
                                secondBook.getId()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Each member can borrow only one book at a time"
                ));

        Book unchangedSecondBook = bookRepository
                .findById(secondBook.getId())
                .orElseThrow();

        org.assertj.core.api.Assertions
                .assertThat(
                        unchangedSecondBook
                                .getAvailableQuantity()
                )
                .isEqualTo(1);
    }

    @Test
    void outOfStockBookCannotBeBorrowed()
            throws Exception {
        Book outOfStockBook = createBook(
                "BORROW-BOOK-003",
                "Out Of Stock Book",
                1,
                0,
                true
        );

        mockMvc.perform(post("/api/borrowings")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(member.accessToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d
                                }
                                """.formatted(
                                outOfStockBook.getId()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Book is out of stock"));
    }

    @Test
    void inactiveBookCannotBeBorrowed()
            throws Exception {
        Book inactiveBook = createBook(
                "BORROW-BOOK-004",
                "Inactive Book",
                1,
                1,
                false
        );

        mockMvc.perform(post("/api/borrowings")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(member.accessToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": %d
                                }
                                """.formatted(
                                inactiveBook.getId()
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Book is inactive"));
    }

    @Test
    void returningSameBorrowingTwiceReturnsBadRequest()
            throws Exception {
        JsonNode borrowing = borrow(
                member.accessToken(),
                book.getId()
        );

        long borrowingId =
                borrowing.get("id").asLong();

        returnBook(
                member.accessToken(),
                borrowingId
        );

        mockMvc.perform(post(
                        "/api/borrowings/{id}/return",
                        borrowingId
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(member.accessToken())
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Book has already been returned"
                        ));
    }

    @Test
    void anotherMemberCannotReturnBorrowing()
            throws Exception {
        JsonNode borrowing = borrow(
                member.accessToken(),
                book.getId()
        );

        MemberFixture anotherMember = createMember(
                "another-borrower@test.local",
                "Another Borrower"
        );

        mockMvc.perform(post(
                        "/api/borrowings/{id}/return",
                        borrowing.get("id").asLong()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(
                                        anotherMember.accessToken()
                                )
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReturnMemberBorrowing()
            throws Exception {
        JsonNode borrowing = borrow(
                member.accessToken(),
                book.getId()
        );

        mockMvc.perform(post(
                        "/api/borrowings/{id}/return",
                        borrowing.get("id").asLong()
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("RETURNED"));
    }

    @Test
    void memberSeesOnlyOwnBorrowingHistory()
            throws Exception {
        borrow(member.accessToken(), book.getId());

        MemberFixture anotherMember = createMember(
                "history-borrower@test.local",
                "History Borrower"
        );

        Book anotherBook = createBook(
                "BORROW-BOOK-005",
                "History Borrowing Book",
                1,
                1,
                true
        );

        borrow(
                anotherMember.accessToken(),
                anotherBook.getId()
        );

        mockMvc.perform(get("/api/borrowings/my")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(member.accessToken())
                        )
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements")
                        .value(1))
                .andExpect(jsonPath("$.content[0].memberId")
                        .value(member.profileId()))
                .andExpect(jsonPath("$.content[0].bookId")
                        .value(book.getId()));
    }

    @Test
    void adminCanViewAllBorrowings()
            throws Exception {
        borrow(member.accessToken(), book.getId());

        MemberFixture anotherMember = createMember(
                "admin-list-borrower@test.local",
                "Admin List Borrower"
        );

        Book anotherBook = createBook(
                "BORROW-BOOK-006",
                "Admin List Book",
                1,
                1,
                true
        );

        borrow(
                anotherMember.accessToken(),
                anotherBook.getId()
        );

        mockMvc.perform(get("/api/admin/borrowings")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements")
                        .value(2));
    }

    @Test
    void userCannotViewAdminBorrowingList()
            throws Exception {
        mockMvc.perform(get("/api/admin/borrowings")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidBorrowRequestReturnsValidationError()
            throws Exception {
        mockMvc.perform(post("/api/borrowings")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(member.accessToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.bookId")
                        .exists());
    }

    @Test
    void missingBookReturnsNotFound()
            throws Exception {
        mockMvc.perform(post("/api/borrowings")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(member.accessToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": 99999999
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Book does not exist: 99999999"
                ));
    }

    @Test
    void borrowingPageSizeAboveTenReturnsBadRequest()
            throws Exception {
        mockMvc.perform(get("/api/borrowings/my")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(member.accessToken())
                        )
                        .param("page", "0")
                        .param("size", "11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.size")
                        .exists());
    }

    private JsonNode borrow(
            String accessToken,
            Long bookId
    ) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/borrowings")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(accessToken)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "bookId": %d
                                        }
                                        """.formatted(bookId))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(
                result.getResponse()
                        .getContentAsString()
        );
    }

    private void returnBook(
            String accessToken,
            long borrowingId
    ) throws Exception {
        mockMvc.perform(post(
                        "/api/borrowings/{id}/return",
                        borrowingId
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(accessToken)
                        ))
                .andExpect(status().isOk());
    }

    private MemberFixture createMember(
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
                                          "address": "Borrowing Test Address"
                                        }
                                        """.formatted(
                                        email,
                                        MEMBER_PASSWORD,
                                        fullName
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse()
                        .getContentAsString()
        );

        String accessToken = loginAndGetAccessToken(
                email,
                MEMBER_PASSWORD
        );

        return new MemberFixture(
                response.get("id").asLong(),
                email,
                accessToken
        );
    }

    private Book createBook(
            String isbn,
            String title,
            int totalQuantity,
            int availableQuantity,
            boolean active
    ) {
        Category category = categoryRepository
                .findByNameIgnoreCase("Technology")
                .orElseThrow();

        Author author = authorRepository
                .findByNameIgnoreCase(
                        "Borrowing Test Author"
                )
                .orElseGet(() -> {
                    Author createdAuthor = new Author();
                    createdAuthor.setName(
                            "Borrowing Test Author"
                    );
                    return authorRepository.save(
                            createdAuthor
                    );
                });

        Book createdBook = new Book();
        createdBook.setIsbn(isbn);
        createdBook.setTitle(title);
        createdBook.setDescription(
                "Borrowing integration test"
        );
        createdBook.setPublisher("Test Publisher");
        createdBook.setPublishedDate(
                LocalDate.of(2020, 1, 1)
        );
        createdBook.setTotalQuantity(totalQuantity);
        createdBook.setAvailableQuantity(
                availableQuantity
        );
        createdBook.setActive(active);
        createdBook.setCategory(category);
        createdBook.addAuthor(author);

        return bookRepository.saveAndFlush(createdBook);
    }

    private record MemberFixture(
            long profileId,
            String email,
            String accessToken
    ) {
    }
}
