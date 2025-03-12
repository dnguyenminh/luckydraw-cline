package vn.com.fecredit.app.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Utility class for validation operations and message handling.
 */
@Component
public class ValidationUtils {

    private final Validator validator;
    private final MessageSource messageSource;

    public ValidationUtils(MessageSource messageSource) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
        this.messageSource = messageSource;
    }

    /**
     * Validates an object and returns all constraint violations.
     *
     * @param object The object to validate
     * @param <T> The type of object
     * @return Set of constraint violations
     */
    public <T> Set<ConstraintViolation<T>> validate(T object) {
        return validator.validate(object);
    }

    /**
     * Validates an object and returns error messages with interpolated values.
     *
     * @param object The object to validate
     * @param <T> The type of object
     * @return Map of field names to error messages
     */
    public <T> Map<String, String> validateAndGetMessages(T object) {
        Set<ConstraintViolation<T>> violations = validate(object);
        Map<String, String> errors = new HashMap<>();
        
        for (ConstraintViolation<T> violation : violations) {
            String field = violation.getPropertyPath().toString();
            String message = interpolateMessage(violation.getMessageTemplate(), violation.getConstraintDescriptor().getAttributes());
            errors.put(field, message);
        }
        
        return errors;
    }

    /**
     * Interpolates a message template with its parameters.
     *
     * @param template The message template
     * @param attributes The attributes to interpolate
     * @return The interpolated message
     */
    public String interpolateMessage(String template, Map<String, Object> attributes) {
        if (template == null || !template.startsWith("{")) {
            return template;
        }

        String messageKey = template.substring(1, template.length() - 1);
        return messageSource.getMessage(
            messageKey,
            extractMessageParameters(attributes),
            LocaleContextHolder.getLocale()
        );
    }

    /**
     * Creates a validation error map with a single message.
     *
     * @param field The field name
     * @param messageKey The message key
     * @param params The message parameters
     * @return Map containing the error message
     */
    public Map<String, String> createValidationError(String field, String messageKey, Object... params) {
        String message = messageSource.getMessage(
            messageKey,
            params,
            LocaleContextHolder.getLocale()
        );
        return Collections.singletonMap(field, message);
    }

    /**
     * Extracts message parameters from validation attributes.
     *
     * @param attributes The validation attributes
     * @return Array of parameter values
     */
    private Object[] extractMessageParameters(Map<String, Object> attributes) {
        List<Object> params = new ArrayList<>();
        
        // Common parameter names in validation constraints
        String[] paramNames = {"min", "max", "value", "pattern", "inclusive", "message"};
        
        for (String name : paramNames) {
            if (attributes.containsKey(name)) {
                params.add(attributes.get(name));
            }
        }
        
        return params.toArray();
    }

    /**
     * Validates an object for a specific validation group.
     *
     * @param object The object to validate
     * @param groups The validation groups
     * @param <T> The type of object
     * @return Set of constraint violations
     */
    public <T> Set<ConstraintViolation<T>> validateForGroups(T object, Class<?>... groups) {
        return validator.validate(object, groups);
    }

    /**
     * Validates a specific property of an object.
     *
     * @param object The object containing the property
     * @param propertyName The name of the property
     * @param <T> The type of object
     * @return Set of constraint violations
     */
    public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName) {
        return validator.validateProperty(object, propertyName);
    }
}
