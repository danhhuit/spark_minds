package com.sparkminds.library.common.exception;

public class InvalidPasswordResetTokenException
        extends RuntimeException {

    public InvalidPasswordResetTokenException() {
        super("Password reset token is invalid or expired");
    }
}