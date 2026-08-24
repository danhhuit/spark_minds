package com.sparkminds.library.book.controller;

import com.sparkminds.library.book.entity.Book;
import com.sparkminds.library.book.repository.BookRepository;
import com.sparkminds.library.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class BookCsvImportIntegrationTest
        extends AbstractIntegrationTest {

    private static final String HEADER =
            "isbn,title,description,publisher,"
                    + "publishedDate,totalQuantity,"
                    + "category,authors\n";

    @Autowired
    private BookRepository bookRepository;

    private String adminAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        adminAccessToken =
                loginAsAdminAndGetAccessToken();
    }

    @AfterEach
    void cleanImportedBooks() {
        List.of(
                "CSV-IT-0001",
                "CSV-IT-0002",
                "CSV-ROLLBACK-0001"
        ).forEach(isbn ->
                bookRepository
                        .findByIsbnIgnoreCase(isbn)
                        .map(Book::getId)
                        .ifPresent(bookRepository::deleteById)
        );
    }

    @Test
    void adminCanImportValidCsvFile()
            throws Exception {
        String csv = HEADER
                + "CSV-IT-0001,CSV First Book,"
                + "First description,Test Publisher,"
                + "2020-01-01,4,Technology,"
                + "CSV Author One|CSV Author Two\n"
                + "CSV-IT-0002,CSV Second Book,"
                + "Second description,Test Publisher,"
                + "2021-02-03,2,Science,"
                + "CSV Author Three\n";

        mockMvc.perform(multipart("/api/books/import")
                        .file(csvFile(
                                "books.csv",
                                csv.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        ))
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount")
                        .value(2))
                .andExpect(jsonPath("$.importedIsbns[0]")
                        .value("CSV-IT-0001"))
                .andExpect(jsonPath("$.importedIsbns[1]")
                        .value("CSV-IT-0002"));

        assertThat(bookRepository.existsByIsbnIgnoreCase(
                "CSV-IT-0001"
        )).isTrue();
        assertThat(bookRepository.existsByIsbnIgnoreCase(
                "CSV-IT-0002"
        )).isTrue();
    }

    @Test
    void csvImportRejectsWrongExtensionAndMissingHeaders()
            throws Exception {
        mockMvc.perform(multipart("/api/books/import")
                        .file(csvFile(
                                "books.txt",
                                HEADER.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        ))
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Only .csv files are supported"
                ));

        String missingHeaders =
                "isbn,title\nCSV-INVALID,Invalid\n";

        mockMvc.perform(multipart("/api/books/import")
                        .file(csvFile(
                                "books.csv",
                                missingHeaders.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        ))
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers
                                .containsString(
                                        "CSV is missing "
                                                + "required headers"
                                )));
    }

    @Test
    void csvImportRejectsFileLargerThanFiveMegabytes()
            throws Exception {
        byte[] oversized =
                new byte[5 * 1024 * 1024 + 1];

        mockMvc.perform(multipart("/api/books/import")
                        .file(csvFile(
                                "oversized.csv",
                                oversized
                        ))
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "CSV file size must not exceed 5 MB"
                ));
    }

    @Test
    void invalidRowRollsBackPreviouslyImportedRows()
            throws Exception {
        String csv = HEADER
                + "CSV-ROLLBACK-0001,Temporary Book,"
                + "Should be rolled back,Publisher,"
                + "2020-01-01,1,Technology,"
                + "Rollback Author\n"
                + "CSV-ROLLBACK-0002,Invalid Book,"
                + "Invalid category,Publisher,"
                + "2020-01-01,1,Missing Category,"
                + "Rollback Author\n";

        mockMvc.perform(multipart("/api/books/import")
                        .file(csvFile(
                                "rollback.csv",
                                csv.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        ))
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(adminAccessToken)
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers
                                .containsString(
                                        "Category does not exist"
                                )));

        assertThat(bookRepository.existsByIsbnIgnoreCase(
                "CSV-ROLLBACK-0001"
        )).isFalse();
        assertThat(bookRepository.existsByIsbnIgnoreCase(
                "CSV-ROLLBACK-0002"
        )).isFalse();
    }

    @Test
    void userCannotImportBooks()
            throws Exception {
        String csv = HEADER
                + "CSV-FORBIDDEN,Forbidden Book,"
                + "Description,Publisher,2020-01-01,"
                + "1,Technology,Author\n";

        mockMvc.perform(multipart("/api/books/import")
                        .file(csvFile(
                                "books.csv",
                                csv.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        ))
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )))
                .andExpect(status().isForbidden());
    }

    private MockMultipartFile csvFile(
            String filename,
            byte[] content
    ) {
        return new MockMultipartFile(
                "file",
                filename,
                "text/csv",
                content
        );
    }
}
