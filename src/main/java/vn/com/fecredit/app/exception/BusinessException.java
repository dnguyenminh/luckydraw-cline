package vn.com.fecredit.app.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final Object[] params;
    private String status;

    public BusinessException(String message) {
        this(message, null, null);
    }

    public BusinessException(String message, String errorCode) {
        this(message, errorCode, null);
    }

    public BusinessException(String message, String errorCode, Object[] params) {
        super(message);
        this.errorCode = errorCode;
        this.params = params;
        this.status = "ERROR";
    }

    protected void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String getCode() {
        return errorCode;
    }

    public static BusinessException invalidRequest(String message) {
        return new BusinessException(message, "INVALID_REQUEST");
    }

    public static BusinessException notFound(String entity, Object id) {
        return new BusinessException(
            String.format("%s with id %s not found", entity, id),
            "NOT_FOUND",
            new Object[]{entity, id}
        );
    }

    public static BusinessException accessDenied(String message) {
        return new BusinessException(message, "ACCESS_DENIED");
    }

    public static BusinessException validationError(String field, String message) {
        return new BusinessException(
            String.format("Validation failed for field '%s': %s", field, message),
            "VALIDATION_ERROR",
            new Object[]{field, message}
        );
    }

    public static BusinessException technicalError(String message) {
        return new BusinessException(message, "TECHNICAL_ERROR");
    }

    public static BusinessException alreadyExists(String entity, String field, Object value) {
        return new BusinessException(
            String.format("%s with %s '%s' already exists", entity, field, value),
            "ALREADY_EXISTS",
            new Object[]{entity, field, value}
        );
    }

    public static BusinessException spinNotAllowed(String reason) {
        return new BusinessException(
            String.format("Spin not allowed: %s", reason),
            "SPIN_NOT_ALLOWED",
            new Object[]{reason}
        );
    }

    public static BusinessException invalidState(String entity, Object id, String expectedState, String actualState) {
        return new BusinessException(
            String.format("%s with id %s is in invalid state. Expected: %s, Actual: %s",
                entity, id, expectedState, actualState),
            "INVALID_STATE",
            new Object[]{entity, id, expectedState, actualState}
        );
    }

    public static BusinessException quotaExceeded(String entity, String quota) {
        return new BusinessException(
            String.format("%s quota exceeded: %s", entity, quota),
            "QUOTA_EXCEEDED",
            new Object[]{entity, quota}
        );
    }

    public static BusinessException timeConstraintViolation(String message) {
        return new BusinessException(message, "TIME_CONSTRAINT_VIOLATION");
    }
}
