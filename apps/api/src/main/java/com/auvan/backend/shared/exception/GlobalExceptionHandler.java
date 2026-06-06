package com.auvan.backend.shared.exception;

import com.auvan.backend.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Handles all AU Van application errors (404, 409, 400, etc.) */
    @ExceptionHandler(AuVanException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuVanException(AuVanException ex) {
        log.debug("Application error [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    /** Handles @Valid / @Validated DTO validation failures → 422 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, List<String>> fieldErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        error -> error instanceof FieldError fe ? fe.getField() : error.getObjectName(),
                        Collectors.mapping(error -> {
                            String msg = error.getDefaultMessage();
                            return msg != null ? msg : "Invalid value";
                        }, Collectors.toList())
                ));

        return ResponseEntity
                .status(HttpStatus.valueOf(422))
                .body(ApiResponse.validationError(fieldErrors));
    }

    /** Spring Security throws this when the user lacks a role */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You do not have permission to perform this action", "FORBIDDEN"));
    }

    /** Spring Security authentication failures */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required", "UNAUTHORIZED"));
    }

    /** Catch-all — log and return a generic 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
    }
}
