package com.sparkminds.library.book.controller;

import com.sparkminds.library.book.repository.BookRepository;
import com.sparkminds.library.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@Transactional
class CatalogSeedIntegrationTest
        extends AbstractIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void migrationProvidesFiftyDetailedBooks() {
        assertThat(bookRepository.count())
                .isGreaterThanOrEqualTo(50);

        assertThat(
                bookRepository
                    .findByIsbnIgnoreCase("978-0132350884")
        )
                .isPresent()
                .get()
                .satisfies(book -> {
                    assertThat(book.getTitle())
                            .isEqualTo("Clean Code");
                    assertThat(book.getDescription())
                            .hasSizeGreaterThan(200);
                    assertThat(book.getPublisher())
                            .isNotBlank();
                    assertThat(book.getPublishedDate())
                            .isNotNull();
                    assertThat(book.getCategory())
                            .isNotNull();
                    assertThat(book.getAuthors())
                            .isNotEmpty();
                });
    }
}
