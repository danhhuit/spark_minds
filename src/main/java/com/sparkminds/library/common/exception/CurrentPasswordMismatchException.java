package com.sparkminds.library.common.exception;

public class CurrentPasswordMismatchException
        extends RuntimeException {

    public CurrentPasswordMismatchException() {
        super("Current password is incorrect");
    }
}