package com.epic.cms.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public GlobalExceptionHandler() {
        logger.info("GlobalExceptionHandler initialized");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.warn("ResourceNotFoundException: {} - URI: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse response = new ErrorResponse("NOT_FOUND", ex.getMessage());
        MDC.clear();
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.warn("BusinessException: {} - URI: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse response = new ErrorResponse("BUSINESS_ERROR", ex.getMessage());
        MDC.clear();
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.warn("IllegalArgumentException: {} - URI: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse response = new ErrorResponse("INVALID_ARGUMENT", ex.getMessage());
        MDC.clear();
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(org.springframework.dao.DataAccessException ex, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.error("DataAccessException: {} - URI: {}", ex.getMessage(), request.getRequestURI(), ex);
        
        ErrorResponse response = new ErrorResponse("DATABASE_ERROR", "Database operation failed");
        MDC.clear();
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        logger.error("Unhandled exception: {} - URI: {}", ex.getMessage(), request.getRequestURI(), ex);
        
        ErrorResponse response = new ErrorResponse("INTERNAL_ERROR", "Unexpected system error occurred");
        MDC.clear();
        return ResponseEntity.ok(response);
    }
}
