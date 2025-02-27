package vn.com.fecredit.app.util;

import lombok.experimental.UtilityClass;
import vn.com.fecredit.app.exception.BusinessException;

import java.util.Collection;
import java.util.regex.Pattern;

@UtilityClass
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[0-9]{10,15}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]{3,30}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number format
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate username format
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validate required field
     */
    public static void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessException(fieldName + " is required");
        }
        if (value instanceof String && ((String) value).trim().isEmpty()) {
            throw new BusinessException(fieldName + " cannot be empty");
        }
        if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
            throw new BusinessException(fieldName + " cannot be empty");
        }
    }

    /**
     * Validate min length
     */
    public static void validateMinLength(String value, int minLength, String fieldName) {
        if (value != null && value.length() < minLength) {
            throw new BusinessException(fieldName + " must be at least " + minLength + " characters long");
        }
    }

    /**
     * Validate max length
     */
    public static void validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new BusinessException(fieldName + " must not exceed " + maxLength + " characters");
        }
    }

    /**
     * Validate min value
     */
    public static void validateMinValue(Number value, Number minValue, String fieldName) {
        if (value != null && value.doubleValue() < minValue.doubleValue()) {
            throw new BusinessException(fieldName + " must be at least " + minValue);
        }
    }

    /**
     * Validate max value
     */
    public static void validateMaxValue(Number value, Number maxValue, String fieldName) {
        if (value != null && value.doubleValue() > maxValue.doubleValue()) {
            throw new BusinessException(fieldName + " must not exceed " + maxValue);
        }
    }

    /**
     * Validate range
     */
    public static void validateRange(Number value, Number minValue, Number maxValue, String fieldName) {
        validateMinValue(value, minValue, fieldName);
        validateMaxValue(value, maxValue, fieldName);
    }

    /**
     * Validate probability value (0.0 to 1.0)
     */
    public static void validateProbability(Double probability, String fieldName) {
        if (probability != null) {
            validateRange(probability, 0.0, 1.0, fieldName);
        }
    }

    /**
     * Validate multiplier value (must be >= 1.0)
     */
    public static void validateMultiplier(Double multiplier, String fieldName) {
        if (multiplier != null && multiplier < 1.0) {
            throw new BusinessException(fieldName + " must be greater than or equal to 1.0");
        }
    }

    /**
     * Validate non-negative value
     */
    public static void validateNonNegative(Number value, String fieldName) {
        if (value != null && value.doubleValue() < 0) {
            throw new BusinessException(fieldName + " must be non-negative");
        }
    }

    /**
     * Validate positive value
     */
    public static void validatePositive(Number value, String fieldName) {
        if (value != null && value.doubleValue() <= 0) {
            throw new BusinessException(fieldName + " must be positive");
        }
    }

    /**
     * Validate time sequence
     */
    public static void validateTimeSequence(java.time.LocalDateTime start, 
            java.time.LocalDateTime end, String startFieldName, String endFieldName) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new BusinessException(endFieldName + " must be after " + startFieldName);
        }
    }
}
