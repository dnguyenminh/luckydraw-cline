package vn.com.fecredit.app.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Standard error response object for the application.
 * Provides consistent error response structure across all endpoints.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ErrorResponse {
    private final String referenceId;
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, String> errors;

    public ErrorResponse(String referenceId, LocalDateTime timestamp, int status, 
                        String error, String message, String path, Map<String, String> errors) {
        this.referenceId = referenceId != null ? referenceId : generateReferenceId();
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    // Constructor for simple errors
    public ErrorResponse(int status, String error, String message) {
        this(null, null, status, error, message, null, null);
    }

    private String generateReferenceId() {
        return "ERR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Static factory methods for common use cases
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message);
    }

    public static ErrorResponse of(int status, String error, String message, Map<String, String> errors) {
        return ErrorResponse.builder()
                .referenceId(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .errors(errors)
                .build();
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .referenceId(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    public static ErrorResponse of(int status, String error, String message, String path, Map<String, String> errors) {
        return ErrorResponse.builder()
                .referenceId(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .errors(errors)
                .build();
    }
}