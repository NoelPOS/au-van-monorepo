package com.auvan.backend.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Unified API response envelope for all endpoints.
 * Fields with null values are omitted from JSON serialisation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        String error,
        String errorCode,
        Map<String, List<String>> validationErrors
) {

    // ---- Success variants ------------------------------------------------

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null, null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null, null, null);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, null, message, null, null, null);
    }

    // ---- Error variants --------------------------------------------------

    public static ApiResponse<Void> error(String errorMessage) {
        return new ApiResponse<>(false, null, null, errorMessage, null, null);
    }

    public static ApiResponse<Void> error(String errorMessage, String errorCode) {
        return new ApiResponse<>(false, null, null, errorMessage, errorCode, null);
    }

    public static ApiResponse<Void> validationError(Map<String, List<String>> validationErrors) {
        return new ApiResponse<>(false, null, "Validation failed", null, "VALIDATION_ERROR", validationErrors);
    }
}
