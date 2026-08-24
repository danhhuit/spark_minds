package com.sparkminds.library.book.dto.response;

import java.util.List;

public record BookImportResponse(
        int importedCount,
        List<String> importedIsbns
) {
}