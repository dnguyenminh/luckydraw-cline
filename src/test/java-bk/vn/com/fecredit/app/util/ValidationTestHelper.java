package vn.com.fecredit.app.util;

import jakarta.validation.ConstraintViolation;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.validation.ValidationUtils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper class for validation testing with utility methods to simplify validation assertions.
 */
@Component
public class ValidationTestHelper {

    private final ValidationUtils validationUtils;

    public ValidationTestHelper(ValidationUtils validationUtils) {
        this.validationUtils = validationUtils;
    }

    /**
     * Validates an object and returns violations for a specific field.
     *
     * @param object The object to validate
     * @param fieldName The field name to check
     * @param <T> The type of object
     * @return Set of violations for the specified field
     */
    public <T> Set<String> validateField(T object, String fieldName) {
        Set<ConstraintViolation<T>> violations = validationUtils.validateProperty(object, fieldName);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    /**
     * Validates an object and checks if it has any violations.
     *
     * @param object The object to validate
     * @param <T> The type of object
     * @return true if the object has no violations
     */
    public <T> boolean isValid(T object) {
        return validationUtils.validate(object).isEmpty();
    }

    /**
     * Validates an object and returns the number of violations.
     *
     * @param object The object to validate
     * @param <T> The type of object
     * @return The number of violations
     */
    public <T> int getViolationCount(T object) {
        return validationUtils.validate(object).size();
    }

    /**
     * Checks if a specific field has a specific violation message.
     *
     * @param object The object to validate
     * @param fieldName The field name to check
     * @param expectedMessage The expected violation message
     * @param <T> The type of object
     * @return true if the field has the expected violation message
     */
    public <T> boolean hasViolation(T object, String fieldName, String expectedMessage) {
        Map<String, String> errors = validationUtils.validateAndGetMessages(object);
        String actualMessage = errors.get(fieldName);
        return actualMessage != null && actualMessage.contains(expectedMessage);
    }

    /**
     * Gets all violation messages for an object.
     *
     * @param object The object to validate
     * @param <T> The type of object
     * @return Map of field names to violation messages
     */
    public <T> Map<String, Set<String>> getAllViolations(T object) {
        Set<ConstraintViolation<T>> violations = validationUtils.validate(object);
        return violations.stream()
                .collect(Collectors.groupingBy(
                    v -> v.getPropertyPath().toString(),
                    Collectors.mapping(
                        ConstraintViolation::getMessage,
                        Collectors.toSet()
                    )
                ));
    }

    /**
     * Gets violation messages containing a specific text.
     *
     * @param object The object to validate
     * @param messageText The text to search for in violation messages
     * @param <T> The type of object
     * @return Set of matching violation messages
     */
    public <T> Set<String> getViolationsContaining(T object, String messageText) {
        return validationUtils.validate(object).stream()
                .map(ConstraintViolation::getMessage)
                .filter(message -> message.contains(messageText))
                .collect(Collectors.toSet());
    }

    /**
     * Checks if a field has any violations.
     *
     * @param object The object to validate
     * @param fieldName The field name to check
     * @param <T> The type of object
     * @return true if the field has any violations
     */
    public <T> boolean hasViolations(T object, String fieldName) {
        return !validateField(object, fieldName).isEmpty();
    }

    /**
     * Gets the first violation message for a field.
     *
     * @param object The object to validate
     * @param fieldName The field name to check
     * @param <T> The type of object
     * @return The first violation message or null if none exists
     */
    public <T> String getFirstViolation(T object, String fieldName) {
        Set<String> violations = validateField(object, fieldName);
        return violations.isEmpty() ? null : violations.iterator().next();
    }

    /**
     * Validates an object for specific validation groups.
     *
     * @param object The object to validate
     * @param groups The validation groups
     * @param <T> The type of object
     * @return Map of field names to violation messages
     */
    public <T> Map<String, String> validateGroups(T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validationUtils.validateForGroups(object, groups);
        return violations.stream()
                .collect(Collectors.toMap(
                    v -> v.getPropertyPath().toString(),
                    ConstraintViolation::getMessage,
                    (msg1, msg2) -> msg1 // Keep first message in case of duplicates
                ));
    }
}
