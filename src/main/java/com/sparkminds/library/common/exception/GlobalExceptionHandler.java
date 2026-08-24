package com.sparkminds.library.common.exception;

import com.sparkminds.library.common.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiErrorResponse> handleAuthentication(
                        AuthenticationException exception,
                        HttpServletRequest request) {
                return build(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid username, password, or account status",
                                request,
                                Map.of());
        }

        @ExceptionHandler(ResourceAlreadyExistsException.class)
        public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExists(
                        ResourceAlreadyExistsException exception,
                        HttpServletRequest request) {
                return build(
                                HttpStatus.CONFLICT,
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler(InvalidVerificationTokenException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidVerificationToken(
                        InvalidVerificationTokenException exception,
                        HttpServletRequest request) {
                return build(
                                HttpStatus.BAD_REQUEST,
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler(InvalidPasswordResetTokenException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidPasswordResetToken(
                        InvalidPasswordResetTokenException exception,
                        HttpServletRequest request) {
                return build(
                                HttpStatus.BAD_REQUEST,
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler({
                        CurrentPasswordMismatchException.class,
                        PasswordReuseException.class
        })
        public ResponseEntity<ApiErrorResponse> handlePasswordOperation(
                        RuntimeException exception,
                        HttpServletRequest request) {
                return build(
                                HttpStatus.BAD_REQUEST,
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler(InvalidRefreshTokenException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidRefreshToken(
                        InvalidRefreshTokenException exception,
                        HttpServletRequest request) {
                return build(
                                HttpStatus.UNAUTHORIZED,
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler(InvalidEmailChangeException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidEmailChange(
                        InvalidEmailChangeException exception,
                        HttpServletRequest request) {
                return build(
                                HttpStatus.BAD_REQUEST,
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
                        ResourceNotFoundException exception,
                        HttpServletRequest request) {
                return build(
                                HttpStatus.NOT_FOUND,
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiErrorResponse> handleBusinessException(
                        BusinessException exception,
                        HttpServletRequest request) {
                return build(
                                HttpStatus.BAD_REQUEST,
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {
                Map<String, String> errors = new LinkedHashMap<>();

                exception.getBindingResult()
                                .getFieldErrors()
                                .forEach(error -> errors.putIfAbsent(
                                                error.getField(),
                                                error.getDefaultMessage()));

                return build(
                                HttpStatus.BAD_REQUEST,
                                "Validation failed",
                                request,
                                errors);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
                        ConstraintViolationException exception,
                        HttpServletRequest request) {
                Map<String, String> errors = new LinkedHashMap<>();

                exception.getConstraintViolations()
                                .forEach(violation -> {
                                        String propertyPath = violation
                                                        .getPropertyPath()
                                                        .toString();

                                        int separatorIndex = propertyPath
                                                        .lastIndexOf('.');

                                        String fieldName = separatorIndex >= 0
                                                        ? propertyPath.substring(
                                                                        separatorIndex + 1)
                                                        : propertyPath;

                                        errors.putIfAbsent(
                                                        fieldName,
                                                        violation.getMessage());
                                });

                return build(
                                HttpStatus.BAD_REQUEST,
                                "Validation failed",
                                request,
                                errors);
        }

        @ExceptionHandler(CsvImportException.class)
        public ResponseEntity<ApiErrorResponse> handleCsvImport(
                        CsvImportException exception,
                        HttpServletRequest request) {
                return build(
                                HttpStatus.BAD_REQUEST,
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiErrorResponse> handleFileTooLarge(
                        MaxUploadSizeExceededException exception,
                        HttpServletRequest request) {
                return build(
                                HttpStatus.PAYLOAD_TOO_LARGE,
                                "File size must not exceed 5 MB",
                                request,
                                Map.of());
        }

        private ResponseEntity<ApiErrorResponse> build(
                        HttpStatus status,
                        String message,
                        HttpServletRequest request,
                        Map<String, String> fieldErrors) {
                ApiErrorResponse response = new ApiErrorResponse(
                                Instant.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                message,
                                request.getRequestURI(),
                                fieldErrors);

                return ResponseEntity
                                .status(status)
                                .body(response);
        }
}
