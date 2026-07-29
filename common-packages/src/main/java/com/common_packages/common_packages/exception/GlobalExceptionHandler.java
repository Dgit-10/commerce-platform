package com.common_packages.common_packages.exception;


import com.common_packages.common_packages.dto.ApiResponse;
import com.common_packages.common_packages.util.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handle 404 - Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseBuilder.error(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 2. Handle 409 - Conflict (User already exists)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.warn("User conflict: {}", ex.getMessage());
        return ResponseBuilder.error(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // 3. Handle @Valid validation errors (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("Validation failed: {}", errors);

        // Pass map of invalid fields or a message
        return ResponseBuilder.error("Validation failed: " + errors, HttpStatus.BAD_REQUEST);
    }

    // 4. Fallback Catch-All - 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        log.error("Unhandled Exception occurred: ", ex);
        return ResponseBuilder.error("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}