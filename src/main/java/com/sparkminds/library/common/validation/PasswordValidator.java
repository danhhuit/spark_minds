package com.sparkminds.library.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class PasswordValidator
        implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)"
                + "(?=.*[@$!%*?&])"
                + "[A-Za-z\\d@$!%*?&]{8,72}$"
            );

    @Override
    public boolean isValid(
            String password,
            ConstraintValidatorContext context
    ) {
        if (password == null || password.isBlank()) {
            return false;
        }

        int byteLength = password
                .getBytes(StandardCharsets.UTF_8)
                .length;

        return byteLength <= 72
                && PASSWORD_PATTERN
                    .matcher(password)
                    .matches();
    }
}