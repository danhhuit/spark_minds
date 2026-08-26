package com.sparkminds.library.book.service;

import com.sparkminds.library.book.dto.request.CreateBookRequest;
import com.sparkminds.library.book.dto.response.BookImportResponse;
import com.sparkminds.library.book.entity.Category;
import com.sparkminds.library.book.repository.CategoryRepository;
import com.sparkminds.library.common.exception.CsvImportException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookCsvImportService {

        private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

        private static final Set<String> REQUIRED_HEADERS = Set.of(
                        "isbn",
                        "title",
                        "description",
                        "publisher",
                        "publishedDate",
                        "totalQuantity",
                        "category",
                        "authors");

        private final BookService bookService;
        private final CategoryRepository categoryRepository;

        @Transactional(rollbackFor = Exception.class)
        public BookImportResponse importBooks(
                        MultipartFile file) {
                validateFile(file);

                List<String> importedIsbns = new ArrayList<>();

                Set<String> fileIsbns = new HashSet<>();

                CSVFormat format = CSVFormat.DEFAULT
                                .builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setIgnoreEmptyLines(true)
                                .setTrim(true)
                                .get();

                try (
                                BufferedReader reader = createUtf8Reader(file);
                                CSVParser parser = format.parse(reader)) {
                        validateHeaders(parser);

                        for (CSVRecord record : parser) {
                                long lineNumber = record.getRecordNumber() + 1;

                                try {
                                        CreateBookRequest request = parseRecord(
                                                        record,
                                                        lineNumber,
                                                        fileIsbns);

                                        bookService.create(request);

                                        importedIsbns.add(
                                                        request.isbn());
                                } catch (CsvImportException exception) {
                                        throw exception;
                                } catch (RuntimeException exception) {
                                        throw new CsvImportException(
                                                        "CSV line "
                                                                        + lineNumber
                                                                        + ": "
                                                                        + exception.getMessage(),
                                                        exception);
                                }
                        }

                        if (importedIsbns.isEmpty()) {
                                throw new CsvImportException(
                                                "CSV file does not contain any data rows");
                        }

                        return new BookImportResponse(
                                        importedIsbns.size(),
                                        List.copyOf(importedIsbns));
                } catch (IOException exception) {
                        throw new CsvImportException(
                                        "Cannot read CSV file",
                                        exception);
                }
        }

        private BufferedReader createUtf8Reader(
                        MultipartFile file) throws IOException {
                BufferedReader reader = new BufferedReader(
                                new InputStreamReader(
                                                file.getInputStream(),
                                                StandardCharsets.UTF_8));

                // Bỏ UTF-8 BOM do một số phiên bản Excel tạo ra.
                reader.mark(1);

                int firstCharacter = reader.read();

                if (firstCharacter != 0xFEFF) {
                        reader.reset();
                }

                return reader;
        }

        private void validateFile(MultipartFile file) {
                if (file == null || file.isEmpty()) {
                        throw new CsvImportException(
                                        "CSV file is required");
                }

                if (file.getSize() > MAX_FILE_SIZE) {
                        throw new CsvImportException(
                                        "CSV file size must not exceed 5 MB");
                }

                String filename = file.getOriginalFilename();

                if (filename == null
                                || !filename.toLowerCase(Locale.ROOT)
                                                .endsWith(".csv")) {
                        throw new CsvImportException(
                                        "Only .csv files are supported");
                }
        }

        private void validateHeaders(CSVParser parser) {
                Set<String> actualHeaders = parser.getHeaderMap().keySet();

                if (!actualHeaders.containsAll(REQUIRED_HEADERS)) {
                        Set<String> missingHeaders = new HashSet<>(REQUIRED_HEADERS);

                        missingHeaders.removeAll(actualHeaders);

                        throw new CsvImportException(
                                        "CSV is missing required headers: "
                                                        + missingHeaders);
                }
        }

        private CreateBookRequest parseRecord(
                        CSVRecord record,
                        long lineNumber,
                        Set<String> fileIsbns) {
                String isbn = required(
                                record,
                                "isbn",
                                lineNumber).toUpperCase(Locale.ROOT);

                validateLength(
                                isbn,
                                20,
                                "isbn",
                                lineNumber);

                if (!fileIsbns.add(isbn)) {
                        throw lineError(
                                        lineNumber,
                                        "Duplicate ISBN inside CSV: " + isbn);
                }

                String title = required(
                                record,
                                "title",
                                lineNumber);

                validateLength(
                                title,
                                255,
                                "title",
                                lineNumber);

                String description = optional(
                                record,
                                "description");

                validateLength(
                                description,
                                2000,
                                "description",
                                lineNumber);

                String publisher = optional(
                                record,
                                "publisher");

                validateLength(
                                publisher,
                                255,
                                "publisher",
                                lineNumber);

                LocalDate publishedDate = parsePublishedDate(
                                optional(record, "publishedDate"),
                                lineNumber);

                int totalQuantity = parseQuantity(
                                required(
                                                record,
                                                "totalQuantity",
                                                lineNumber),
                                lineNumber);

                String categoryName = required(
                                record,
                                "category",
                                lineNumber);

                Category category = categoryRepository
                                .findByNameIgnoreCase(categoryName)
                                .orElseThrow(() -> lineError(
                                                lineNumber,
                                                "Category does not exist: "
                                                                + categoryName));

                if (!category.isActive()) {
                        throw lineError(
                                        lineNumber,
                                        "Category is inactive: "
                                                        + categoryName);
                }

                Set<String> authors = parseAuthors(
                                required(
                                                record,
                                                "authors",
                                                lineNumber),
                                lineNumber);

                return new CreateBookRequest(
                                isbn,
                                title,
                                description,
                                publisher,
                                publishedDate,
                                totalQuantity,
                                category.getId(),
                                authors);
        }

        private Set<String> parseAuthors(
                        String value,
                        long lineNumber) {
                Set<String> authors = new LinkedHashSet<>();

                for (String authorName : value.split("\\|")) {
                        String normalizedName = authorName.trim();

                        if (normalizedName.isBlank()) {
                                continue;
                        }

                        validateLength(
                                        normalizedName,
                                        150,
                                        "author",
                                        lineNumber);

                        authors.add(normalizedName);
                }

                if (authors.isEmpty()) {
                        throw lineError(
                                        lineNumber,
                                        "At least one author is required");
                }

                return authors;
        }

        private LocalDate parsePublishedDate(
                        String value,
                        long lineNumber) {
                if (value == null) {
                        return null;
                }

                try {
                        LocalDate date = LocalDate.parse(value);

                        if (date.isAfter(LocalDate.now())) {
                                throw lineError(
                                                lineNumber,
                                                "Published date cannot be in the future");
                        }

                        return date;
                } catch (DateTimeParseException exception) {
                        throw lineError(
                                        lineNumber,
                                        "publishedDate must use yyyy-MM-dd format");
                }
        }

        private int parseQuantity(
                        String value,
                        long lineNumber) {
                try {
                        int quantity = Integer.parseInt(value);

                        if (quantity < 0) {
                                throw lineError(
                                                lineNumber,
                                                "totalQuantity cannot be negative");
                        }

                        return quantity;
                } catch (NumberFormatException exception) {
                        throw lineError(
                                        lineNumber,
                                        "totalQuantity must be an integer");
                }
        }

        private String required(
                        CSVRecord record,
                        String column,
                        long lineNumber) {
                String value = optional(record, column);

                if (value == null) {
                        throw lineError(
                                        lineNumber,
                                        column + " is required");
                }

                return value;
        }

        private String optional(
                        CSVRecord record,
                        String column) {
                String value = record.get(column);

                if (value == null || value.isBlank()) {
                        return null;
                }

                return value.trim();
        }

        private void validateLength(
                        String value,
                        int maximumLength,
                        String field,
                        long lineNumber) {
                if (value != null
                                && value.length() > maximumLength) {
                        throw lineError(
                                        lineNumber,
                                        field
                                                        + " must contain at most "
                                                        + maximumLength
                                                        + " characters");
                }
        }

        private CsvImportException lineError(
                        long lineNumber,
                        String message) {
                return new CsvImportException(
                                "CSV line "
                                                + lineNumber
                                                + ": "
                                                + message);
        }
}