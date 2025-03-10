package com.example.versjon2;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private <T> ResponseEntity<APIResponse<T>> buildResponse(HttpStatus status, String message, T data) {
        APIResponse<T> response = APIResponse.<T>builder()
                .success(false)
                .message(message)
                .status(status)
                .data(data)
                .build();
        return ResponseEntity.status(status)
                .headers(SecurityConfig.createSecurityHeaders())
                .body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<APIResponse<List<String>>> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        logger.warn("ConstraintViolationException: {}", errors, ex); // Enkel logging
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<APIResponse<List<String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.toList());
        logger.warn("MethodArgumentNotValidException: {}", errors, ex); // Enkel logging
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<APIResponse<ErrorInfo>> handleGeneralException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorInfo errorInfo = new ErrorInfo("Internal Server Error", "An unexpected error occured.",
                ex.getClass().getSimpleName());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occured.", errorInfo);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<APIResponse<ErrorInfo>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("IllegalArgumentException: {}", ex.getMessage(), ex);
        ErrorInfo errorInfo = new ErrorInfo("Invalid argument", "The provided argument is invalid",
                ex.getClass().getSimpleName());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), errorInfo);
    }

    @ExceptionHandler(DataAccessException.class)
    protected ResponseEntity<APIResponse<ErrorInfo>> handleDataAccessException(DataAccessException ex) {
        logger.error("DataAccessException: {}", ex.getMessage(), ex);
        ErrorInfo errorInfo = new ErrorInfo("Database Error", "An error occurred while accessing the database.",
                ex.getClass().getSimpleName());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database error", errorInfo);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    protected ResponseEntity<APIResponse<ErrorInfo>> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex) {
        logger.info("EmptyResultDataAccessException: {}", ex.getMessage(), ex);
        ErrorInfo errorInfo = new ErrorInfo("Data Not Found", ex.getMessage(),
                ex.getClass().getSimpleName());
        return buildResponse(HttpStatus.NOT_FOUND, "Invalid argument", errorInfo);
    }

    @ExceptionHandler(ResponseStatusException.class)
    protected ResponseEntity<APIResponse<ErrorInfo>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = (HttpStatus) ex.getStatusCode();
        if(status.is5xxServerError()){
            logger.error("ResponseStatusException: {}, Status: {}", ex.getMessage(), status, ex); // ERROR
        } else {
            logger.warn("ResponseStatusException: {}, Status: {}", ex.getMessage(), status, ex); // WARN
        }
        ErrorInfo errorInfo = new ErrorInfo(status.getReasonPhrase(), ex.getMessage(), ex.getClass().getSimpleName()); // Bruk status.getReasonPhrase()
        return buildResponse(status, ex.getMessage(), errorInfo);
    }
}
