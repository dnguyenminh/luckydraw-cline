package vn.com.fecredit.app.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RoleValidationException extends RuntimeException {

    private final Map<String, List<String>> violations;
    private final Long roleId;

    public RoleValidationException(String message) {
        super(message);
        this.violations = new HashMap<>();
        this.roleId = null;
    }

    public RoleValidationException(String message, Long roleId) {
        super(message);
        this.violations = new HashMap<>();
        this.roleId = roleId;
    }

    public RoleValidationException(String message, Map<String, List<String>> violations) {
        super(message);
        this.violations = violations;
        this.roleId = null;
    }

    public RoleValidationException(String message, Long roleId, Map<String, List<String>> violations) {
        super(message);
        this.violations = violations;
        this.roleId = roleId;
    }

    public static RoleValidationException fromBindingResult(BindingResult bindingResult) {
        Map<String, List<String>> violations = new HashMap<>();
        
        for (FieldError error : bindingResult.getFieldErrors()) {
            violations.computeIfAbsent(error.getField(), k -> new ArrayList<>())
                     .add(error.getDefaultMessage());
        }

        return new RoleValidationException(
            "Role validation failed",
            violations
        );
    }

    public static RoleValidationException create(String message) {
        return new RoleValidationException(message);
    }

    public static RoleValidationException forRole(Long roleId, String message) {
        return new RoleValidationException(message, roleId);
    }

    public RoleValidationException addViolation(String field, String message) {
        violations.computeIfAbsent(field, k -> new ArrayList<>())
                 .add(message);
        return this;
    }

    public boolean hasViolations() {
        return !violations.isEmpty();
    }

    public int getViolationCount() {
        return violations.values().stream()
                .mapToInt(List::size)
                .sum();
    }
}
