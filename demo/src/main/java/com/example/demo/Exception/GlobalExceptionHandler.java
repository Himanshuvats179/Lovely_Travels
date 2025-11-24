package com.example.demo.Exception; // Recommended package for application-wide config

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.naming.AuthenticationException;
import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j // Uses SLF4J logging
@RestControllerAdvice // Combines @ControllerAdvice and @ResponseBody
public class GlobalExceptionHandler {

    // --- 1. Validation Errors (e.g., @NotNull, @Size constraints) ---
    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        // Collect all field errors into a single message
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.error("Validation Error: {}", errors);

        return new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Input failed validation: " + errors,
                request.getDescription(false).replace("uri=", "")
        );
    }

    // --- 2. Resource Not Found (e.g., Fetching a non-existent Booking or User) ---
    @org.springframework.web.bind.annotation.ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(EntityNotFoundException ex, WebRequest request) {
        log.warn("Resource Not Found: {}", ex.getMessage());
        return new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    // --- 3. Concurrency Conflict (Crucial for Inventory Service) ---
    @org.springframework.web.bind.annotation.ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex, WebRequest request) {
        log.error("Optimistic Lock Failure: {}", ex.getMessage());
        return new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.CONFLICT.value(),
                "Concurrency Conflict",
                "Inventory or data was modified by another user. Please retry the operation.",
                request.getDescription(false).replace("uri=", "")
        );
    }

    // --- 4. Database Constraint Violation (e.g., Duplicate email/phone, FK violation) ---
    @org.springframework.web.bind.annotation.ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        String detailedMessage = "Data integrity violation. This usually means a unique field (like email) is duplicated, or a foreign key constraint failed.";

        // Try to be more specific for unique constraint errors
        if (ex.getCause() instanceof ConstraintViolationException) {
            detailedMessage = "Unique constraint violation: " + ((ConstraintViolationException) ex.getCause()).getConstraintName();
        }

        log.error("Data Integrity Error: {}", rootCause);

        return new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.CONFLICT.value(),
                "Data Integrity Error",
                detailedMessage,
                request.getDescription(false).replace("uri=", "")
        );
    }
    @ExceptionHandler({RuntimeException.class, java.lang.Exception.class}) // FIX 1: Catch java.lang.Exception
    public ResponseEntity<ErrorResponse> handleAllExceptions(java.lang.Exception ex, WebRequest request) { // FIX 2: Parameter is java.lang.Exception

        // 1. Log the error internally
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);

        // 2. Build your custom ErrorResponse object
        // NOTE: Ensure ErrorResponse is imported or in the same package!
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. We are looking into it.",
                request.getDescription(false).replace("uri=", "")
        );

        // 3. Return the ResponseEntity with the 500 status code
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // --- NEW HANDLER 5: Custom Authentication Errors (401 Unauthorized) ---
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // Use 401 for typical auth failure
    public ErrorResponse handleCustomAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.error("Authentication Error: {}", ex.getMessage());
        return new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Error",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
    }

    @ExceptionHandler(ExternalAuthServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY) // Use 502 when a third-party service fails
    public ErrorResponse handleExternalServiceException(ExternalAuthServiceException ex, WebRequest request) {
        log.error("External Service Error: {}", ex.getMessage());
        return new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.BAD_GATEWAY.value(),
                "External Service Error",
                "Failed to communicate with the external authentication provider (Google).",
                request.getDescription(false).replace("uri=", "")
        );
    }
}