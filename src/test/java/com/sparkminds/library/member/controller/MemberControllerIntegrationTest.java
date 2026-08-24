package com.sparkminds.library.member.controller;

import com.sparkminds.library.book.entity.Author;
import com.sparkminds.library.book.entity.Book;
import com.sparkminds.library.book.entity.Category;
import com.sparkminds.library.book.repository.AuthorRepository;
import com.sparkminds.library.book.repository.BookRepository;
import com.sparkminds.library.book.repository.CategoryRepository;
import com.sparkminds.library.borrowing.entity.Borrowing;
import com.sparkminds.library.borrowing.entity.BorrowingStatus;
import com.sparkminds.library.borrowing.repository.BorrowingRepository;
import com.sparkminds.library.integration.AbstractIntegrationTest;
import com.sparkminds.library.member.entity.MemberProfile;
import com.sparkminds.library.member.repository.MemberProfileRepository;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
class MemberControllerIntegrationTest
        extends AbstractIntegrationTest {

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    private String adminAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        adminAccessToken =
                loginAsAdminAndGetAccessToken();
    }

    @Test
    void adminCanCreateMember() throws Exception {
        mockMvc.perform(post("/api/admin/members")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createMemberJson(
                                "member-create@test.local",
                                "Nguyen Van Create",
                                "1998-04-15"
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("member-create@test.local"))
                .andExpect(jsonPath("$.username")
                        .value("member-create@test.local"))
                .andExpect(jsonPath("$.fullName")
                        .value("Nguyen Van Create"))
                .andExpect(jsonPath("$.membershipCode")
                        .isNotEmpty())
                .andExpect(jsonPath("$.enabled")
                        .value(true))
                .andExpect(jsonPath("$.emailVerified")
                        .value(true))
                .andExpect(jsonPath("$.accountNonLocked")
                        .value(true))
                .andExpect(jsonPath("$.roles[0]")
                        .value("USER"));
    }

    @Test
    void invalidMemberReturnsValidationErrors()
            throws Exception {
        mockMvc.perform(post("/api/admin/members")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": "weak",
                                  "fullName": "",
                                  "dateOfBirth": "2035-01-01",
                                  "phone": "123",
                                  "address": "Test"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.email")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.password")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.fullName")
                        .exists())
                .andExpect(jsonPath(
                        "$.fieldErrors.dateOfBirth"
                ).exists())
                .andExpect(jsonPath("$.fieldErrors.phone")
                        .exists());
    }

    @Test
    void duplicateMemberEmailReturnsConflict()
            throws Exception {
        createMemberThroughApi(
                "duplicate-member@test.local",
                "First Member",
                "1995-01-01"
        );

        mockMvc.perform(post("/api/admin/members")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createMemberJson(
                                "duplicate-member@test.local",
                                "Second Member",
                                "1996-01-01"
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Email has already been registered"
                        ));
    }

    @Test
    void userCannotAccessMemberManagement()
            throws Exception {
        mockMvc.perform(get("/api/admin/members")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanUpdateMember() throws Exception {
        JsonNode member = createMemberThroughApi(
                "member-update@test.local",
                "Old Member Name",
                "1994-06-20"
        );

        long memberId = member.get("id").asLong();

        mockMvc.perform(put(
                        "/api/admin/members/{id}",
                        memberId
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Updated Member Name",
                                  "dateOfBirth": "1994-06-20",
                                  "phone": "0901234567",
                                  "address": "Updated Address",
                                  "enabled": false,
                                  "accountNonLocked": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName")
                        .value("Updated Member Name"))
                .andExpect(jsonPath("$.phone")
                        .value("0901234567"))
                .andExpect(jsonPath("$.address")
                        .value("Updated Address"))
                .andExpect(jsonPath("$.enabled")
                        .value(false))
                .andExpect(jsonPath("$.accountNonLocked")
                        .value(false));
    }

    @Test
    void deactivatingMemberDisablesAccount()
            throws Exception {
        JsonNode member = createMemberThroughApi(
                "member-deactivate@test.local",
                "Deactivate Member",
                "1993-03-12"
        );

        long memberId = member.get("id").asLong();

        mockMvc.perform(delete(
                        "/api/admin/members/{id}",
                        memberId
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(
                        "/api/admin/members/{id}",
                        memberId
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled")
                        .value(false));
    }

    @Test
    void searchCombinesMoreThanFiveConditions()
            throws Exception {
        JsonNode expectedMember = createMemberThroughApi(
                "member-search-a@test.local",
                "Nguyen Van Search",
                "1997-08-25"
        );

        createMemberThroughApi(
                "member-search-b@test.local",
                "Tran Thi Other",
                "1985-02-10"
        );

        String membershipCode = expectedMember
                .get("membershipCode")
                .asText();

        mockMvc.perform(get("/api/admin/members")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .param("fullName", "Nguyen Van")
                        .param("email", "member-search-a")
                        .param(
                                "membershipCode",
                                membershipCode
                        )
                        .param(
                                "dateOfBirthFrom",
                                "1990-01-01"
                        )
                        .param(
                                "dateOfBirthTo",
                                "2000-12-31"
                        )
                        .param("enabled", "true")
                        .param("emailVerified", "true")
                        .param(
                                "accountNonLocked",
                                "true"
                        )
                        .param("role", "USER")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements")
                        .value(1))
                .andExpect(jsonPath("$.content[0].email")
                        .value("member-search-a@test.local"))
                .andExpect(jsonPath(
                        "$.content[0].membershipCode"
                ).value(membershipCode));
    }

    @Test
    void searchFindsMemberByBorrowedBook()
            throws Exception {
        JsonNode member = createMemberThroughApi(
                "book-member@test.local",
                "Book Search Member",
                "1996-07-10"
        );

        long memberId = member.get("id").asLong();
        MemberProfile profile = memberProfileRepository
                .findDetailedById(memberId)
                .orElseThrow();

        Book book = createBookForBorrowing();

        OffsetDateTime borrowedAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Borrowing borrowing = new Borrowing();
        borrowing.setMember(profile);
        borrowing.setBook(book);
        borrowing.setStatus(BorrowingStatus.BORROWED);
        borrowing.setBorrowedAt(borrowedAt);
        borrowing.setDueAt(borrowedAt.plusDays(14));

        borrowingRepository.saveAndFlush(borrowing);

        mockMvc.perform(get("/api/admin/members")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .param(
                                "bookTitle",
                                "Member Search Book"
                        )
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements")
                        .value(1))
                .andExpect(jsonPath("$.content[0].id")
                        .value(memberId))
                .andExpect(jsonPath("$.content[0].email")
                        .value("book-member@test.local"));
    }

    @Test
    void invalidDateRangeReturnsBadRequest()
            throws Exception {
        mockMvc.perform(get("/api/admin/members")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .param(
                                "dateOfBirthFrom",
                                "2000-01-01"
                        )
                        .param(
                                "dateOfBirthTo",
                                "1990-01-01"
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Date of birth from must be before date of birth to"
                ));
    }

    @Test
    void memberPageSizeAboveTenReturnsBadRequest()
            throws Exception {
        mockMvc.perform(get("/api/admin/members")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        )
                        .param("page", "0")
                        .param("size", "11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.size")
                        .value(
                                "Each page contains at most 10 records"
                        ));
    }

    @Test
    void missingMemberReturnsNotFound()
            throws Exception {
        mockMvc.perform(get(
                        "/api/admin/members/99999999"
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Member does not exist: 99999999"
                ));
    }

    private JsonNode createMemberThroughApi(
            String email,
            String fullName,
            String dateOfBirth
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
                                .content(createMemberJson(
                                        email,
                                        fullName,
                                        dateOfBirth
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(
                result.getResponse()
                        .getContentAsString()
        );
    }

    private String createMemberJson(
            String email,
            String fullName,
            String dateOfBirth
    ) {
        return """
                {
                  "email": "%s",
                  "password": "Member@123",
                  "fullName": "%s",
                  "dateOfBirth": "%s",
                  "phone": "0912345678",
                  "address": "Test Address"
                }
                """.formatted(
                email,
                fullName,
                dateOfBirth
        );
    }

    private Book createBookForBorrowing() {
        Category category = categoryRepository
                .findByNameIgnoreCase("Technology")
                .orElseThrow();

        Author author = new Author();
        author.setName("Member Search Author");
        author = authorRepository.save(author);

        Book book = new Book();
        book.setIsbn("MEMBER-SEARCH-BOOK");
        book.setTitle("Member Search Book");
        book.setDescription("Member search integration test");
        book.setPublisher("Test Publisher");
        book.setPublishedDate(
                LocalDate.of(2020, 1, 1)
        );
        book.setTotalQuantity(1);
        book.setAvailableQuantity(0);
        book.setActive(true);
        book.setCategory(category);
        book.addAuthor(author);

        return bookRepository.saveAndFlush(book);
    }
}
