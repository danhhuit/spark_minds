package com.sparkminds.library.common.exception;

public class CsvImportException extends RuntimeException {

    public CsvImportException(String message) {
        super(message);
    }

    public CsvImportException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}