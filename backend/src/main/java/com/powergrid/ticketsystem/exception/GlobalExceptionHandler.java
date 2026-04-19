package com.powergrid.ticketsystem.exception;

import com.powergrid.ticketsystem.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * GLOBAL EXCEPTION HANDLER
 * ============================================================
 * 
 * Centralized exception handling for all REST controllers.
 * 
 * PURPOSE:
 * - Provides consistent error response format across all APIs
 * - Handles validation errors gracefully
 * - Logs all exceptions for debugging
 * - Returns appropriate HTTP status codes
 * 
 * BENEFITS:
 * - No need for try-catch in every controller method
 * - Consistent API error response structure
 * - Better client experience with meaningful error messages
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ============================================================
    // VALIDATION EXCEPTION HANDLER
    // ============================================================

    /**
     * Handles validation errors from @Valid annotation.
     * 
     * Triggered when:
     * - Required fields are missing
     * - Field constraints are violated (size, pattern, etc.)
     * 
     * Returns detailed field-level error messages.
     * 
     * @param ex MethodArgumentNotValidException
     * @return 400 Bad Request with validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        logger.warn("Validation error occurred: {}", ex.getMessage());

        // Collect all field errors
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Validation failed. Please check the input fields.");
        response.setData(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ============================================================
    // ILLEGAL ARGUMENT EXCEPTION HANDLER
    // ============================================================

    /**
     * Handles IllegalArgumentException (business logic validation).
     * 
     * Triggered when:
     * - Invalid parameters passed to service methods
     * - Business rule violations
     * 
     * @param ex IllegalArgumentException
     * @return 400 Bad Request with error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        logger.warn("Illegal argument: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ============================================================
    // TYPE MISMATCH EXCEPTION HANDLER
    // ============================================================

    /**
     * Handles type mismatch in request parameters.
     * 
     * Triggered when:
     * - Wrong parameter type in URL path or query
     * - Example: passing string where number expected
     * 
     * @param ex MethodArgumentTypeMismatchException
     * @return 400 Bad Request with error details
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {

        String message = String.format("Invalid value '%s' for parameter '%s'",
                ex.getValue(), ex.getName());
        logger.warn("Type mismatch: {}", message);

        ApiResponse<Object> response = ApiResponse.error(message);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ============================================================
    // GENERIC EXCEPTION HANDLER (Fallback)
    // ============================================================

    /**
     * Handles all other unhandled exceptions.
     * 
     * Acts as a fallback for any exception not specifically handled.
     * Logs full stack trace for debugging while returning
     * a user-friendly message.
     * 
     * @param ex Exception
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {

        logger.error("Unhandled exception occurred: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later.");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
