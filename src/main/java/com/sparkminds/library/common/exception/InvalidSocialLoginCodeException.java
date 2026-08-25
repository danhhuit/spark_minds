package com.sparkminds.library.common.exception;

public class InvalidSocialLoginCodeException
        extends RuntimeException {

    public InvalidSocialLoginCodeException() {
        super("Social login code is invalid or expired");
    }
}
