package com.sparkminds.library.common.exception;

public class InvalidEmailChangeException
        extends RuntimeException {

    public InvalidEmailChangeException(String message) {
        super(message);
    }
}