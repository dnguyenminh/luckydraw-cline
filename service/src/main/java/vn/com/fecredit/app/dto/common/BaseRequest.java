package vn.com.fecredit.app.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import vn.com.fecredit.app.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
public abstract class BaseRequest implements Validator {

    @JsonIgnore
    private final LocalDateTime requestTime = LocalDateTime.now();

    @JsonIgnore
    private final String requestId = java.util.UUID.randomUUID().toString();

    @Override
    public boolean supports(Class<?> clazz) {
        return this.getClass().equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // Default implementation - override in subclasses for custom validation
    }

    /**
     * Custom validation logic to be implemented by subclasses
     */
    protected void validateFields() throws BusinessException {
        // Override in subclasses to add custom validation logic
    }

    /**
     * Validate and throw exception if invalid
     */
    public final void validateRequest() throws BusinessException {
        validateFields();
    }

    /**
     * Get optional value with null check
     */
    protected <T> Optional<T> getOptional(T value) {
        return Optional.ofNullable(value);
    }

    /**
     * Get required value or throw exception
     */
    protected <T> T getRequired(T value, String fieldName) {
        return Optional.ofNullable(value)
                .orElseThrow(() -> new BusinessException(fieldName + " is required"));
    }

    /**
     * Check if string is not blank
     */
    protected boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Get trimmed string or null
     */
    protected String getTrimmedOrNull(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * Check if number is positive
     */
    protected boolean isPositive(Number value) {
        return value != null && value.doubleValue() > 0;
    }

    /**
     * Check if number is not negative
     */
    protected boolean isNotNegative(Number value) {
        return value != null && value.doubleValue() >= 0;
    }

    /**
     * Check if date is future or present
     */
    protected boolean isFutureOrPresent(LocalDateTime date) {
        return date != null && !date.isBefore(LocalDateTime.now());
    }

    /**
     * Check if date is in future
     */
    protected boolean isFuture(LocalDateTime date) {
        return date != null && date.isAfter(LocalDateTime.now());
    }

    /**
     * Check if end date is after start date
     */
    protected boolean isValidDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return startDate != null && endDate != null && endDate.isAfter(startDate);
    }
}
