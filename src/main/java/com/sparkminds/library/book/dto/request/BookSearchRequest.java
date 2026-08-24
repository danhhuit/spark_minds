package com.sparkminds.library.book.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class BookSearchRequest {

    private String keyword;
    private String title;
    private String isbn;
    private String authorName;
    private String publisher;
    private Long categoryId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate publishedFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate publishedTo;

    private Boolean availableOnly;
    private Boolean active;
}