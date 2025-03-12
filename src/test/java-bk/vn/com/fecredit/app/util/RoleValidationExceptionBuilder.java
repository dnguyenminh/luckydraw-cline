package vn.com.fecredit.app.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.validation.AbstractBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import vn.com.fecredit.app.exception.RoleValidationException;

public class RoleValidationExceptionBuilder {
    private String message;
    private Long roleId;
    private final Map<String, List<String>> violations = new HashMap<>();
    private List<FieldError> fieldErrors = new ArrayList<>();

    public static RoleValidationExceptionBuilder builder() {
        return new RoleValidationExceptionBuilder();
    }

    public RoleValidationExceptionBuilder message(String message) {
        this.message = message;
        return this;
    }

    public RoleValidationExceptionBuilder roleId(Long roleId) {
        this.roleId = roleId;
        return this;
    }

    public RoleValidationExceptionBuilder violation(String field, String message) {
        violations.computeIfAbsent(field, k -> new ArrayList<>())
                 .add(message);
        return this;
    }

    public RoleValidationExceptionBuilder fieldError(String field, String message) {
        fieldErrors.add(new FieldError("role", field, message));
        return this;
    }

    public RoleValidationExceptionBuilder violations(Map<String, List<String>> violations) {
        this.violations.putAll(violations);
        return this;
    }

    public RoleValidationException build() {
        if (!fieldErrors.isEmpty()) {
            return buildFromFieldErrors();
        }

        if (roleId != null && !violations.isEmpty()) {
            return new RoleValidationException(message, roleId, new HashMap<>(violations));
        }

        if (roleId != null) {
            return new RoleValidationException(message, roleId);
        }

        if (!violations.isEmpty()) {
            return new RoleValidationException(message, new HashMap<>(violations));
        }

        return new RoleValidationException(message);
    }

    private RoleValidationException buildFromFieldErrors() {
        TestBindingResult bindingResult = new TestBindingResult();
        fieldErrors.forEach(bindingResult::addError);
        return RoleValidationException.fromBindingResult(bindingResult);
    }

    private static class TestBindingResult extends AbstractBindingResult {
        private final List<ObjectError> errors = new ArrayList<>();

        protected TestBindingResult() {
            super("role");
        }

        @Override
        public Object getTarget() {
            return null;
        }

        @Override
        public void addError(ObjectError error) {
            errors.add(error);
        }

        @Override
        protected Object getActualFieldValue(String field) {
            return null;
        }

        @Override
        public PropertyEditorRegistry getPropertyEditorRegistry() {
            return null;
        }

        @Override
        public String[] resolveMessageCodes(String errorCode, String field) {
            return new String[0];
        }

        @Override
        public List<ObjectError> getAllErrors() {
            return errors;
        }

        @Override
        public Object getFieldValue(String field) {
            return null;
        }

        @Override
        public Class<?> getFieldType(String field) {
            return null;
        }
    }
}
