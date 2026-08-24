package com.sparkminds.library.common.exception;

public class InvalidVerificationTokenException
        extends RuntimeException {

    public InvalidVerificationTokenException() {
        super("Verification token is invalid or expired");
    }
}