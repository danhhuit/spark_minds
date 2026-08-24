package com.sparkminds.library.common.exception;

public class PasswordReuseException
        extends RuntimeException {

    public PasswordReuseException() {
        super("New password must be different "
                + "from current password");
    }
}