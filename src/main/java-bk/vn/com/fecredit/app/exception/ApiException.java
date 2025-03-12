package vn.com.fecredit.app.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ApiException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final HttpStatus status;
    private final String message;
    private final String errorCode;
    private final LocalDateTime timestamp;
    private final Map<String, Object> details;

    private ApiException(HttpStatus status, String message, String errorCode) {
        super(message);
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.details = new HashMap<>();
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public ApiException addDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }

    // Factory methods for common API exceptions
    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message, "BAD_REQUEST");
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, message, "UNAUTHORIZED");
    }

    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message, "FORBIDDEN");
    }

    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message, "NOT_FOUND");
    }

    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message, "CONFLICT");
    }

    public static ApiException internalError(String message) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, message, "INTERNAL_ERROR");
    }

    public static ApiException validationError(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message, "VALIDATION_ERROR");
    }

    // Specific API exceptions
    public static ApiException invalidInput(String field, String message) {
        return badRequest("Invalid input")
                .addDetail("field", field)
                .addDetail("message", message);
    }

    public static ApiException accessDenied(String resource) {
        return forbidden("Access denied")
                .addDetail("resource", resource);
    }

    public static ApiException resourceNotFound(String resource, String identifier) {
        return notFound("Resource not found")
                .addDetail("resource", resource)
                .addDetail("identifier", identifier);
    }

    public static ApiException duplicateResource(String resource, String field, String value) {
        return conflict("Duplicate resource")
                .addDetail("resource", resource)
                .addDetail("field", field)
                .addDetail("value", value);
    }

    public static ApiException invalidToken(String tokenType) {
        return unauthorized("Invalid token")
                .addDetail("tokenType", tokenType);
    }

    public static ApiException expiredToken(String tokenType) {
        return unauthorized("Expired token")
                .addDetail("tokenType", tokenType);
    }

    public static ApiException insufficientFunds(String message, double required, double available) {
        return badRequest(message)
                .addDetail("required", required)
                .addDetail("available", available);
    }

    public static ApiException operationFailed(String operation, String reason) {
        return internalError("Operation failed")
                .addDetail("operation", operation)
                .addDetail("reason", reason);
    }

    public static ApiException serviceFailed(String service, String error) {
        return internalError("Service failed")
                .addDetail("service", service)
                .addDetail("error", error);
    }

    public static ApiException tooManyRequests(String resource, int limit, long resetTime) {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests", "RATE_LIMIT_EXCEEDED")
                .addDetail("resource", resource)
                .addDetail("limit", limit)
                .addDetail("resetTime", resetTime);
    }
}
