package vn.com.fecredit.app.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;

import vn.com.fecredit.app.exception.RoleValidationException;

class RoleValidationExceptionBuilderTest {

    @Test
    void shouldBuildWithNoViolationsOrFieldErrors() {
        RoleValidationException exception = RoleValidationExceptionBuilder.builder()
            .message("Test message")
            .build();

        assertThat(exception.getMessage()).isEqualTo("Test message");
        assertThat(exception.getViolations()).isEmpty();
        assertThat(exception.getRoleId()).isNull();
    }

    @Test
    void shouldBuildWithMultipleFieldErrors() {
        RoleValidationException exception = RoleValidationExceptionBuilder.builder()
            .fieldError("name", "Name is required")
            .fieldError("name", "Name is too short")
            .fieldError("description", "Description is too long")
            .build();

        Map<String, List<String>> violations = exception.getViolations();
        assertThat(violations)
            .containsEntry("name", Arrays.asList("Name is required", "Name is too short"))
            .containsEntry("description", Arrays.asList("Description is too long"));
    }

    @Test
    void shouldBuildWithMultipleViolations() {
        Map<String, List<String>> initialViolations = new HashMap<>();
        initialViolations.put("field1", Arrays.asList("Error 1", "Error 2"));
        
        RoleValidationException exception = RoleValidationExceptionBuilder.builder()
            .violations(initialViolations)
            .violation("field2", "Error 3")
            .violation("field2", "Error 4")
            .build();

        Map<String, List<String>> violations = exception.getViolations();
        assertThat(violations)
            .containsEntry("field1", Arrays.asList("Error 1", "Error 2"))
            .containsEntry("field2", Arrays.asList("Error 3", "Error 4"));
    }

    @Test
    void shouldBuildWithRoleIdAndViolations() {
        RoleValidationException exception = RoleValidationExceptionBuilder.builder()
            .message("Invalid role")
            .roleId(123L)
            .violation("name", "Invalid name")
            .build();

        assertThat(exception.getMessage()).isEqualTo("Invalid role");
        assertThat(exception.getRoleId()).isEqualTo(123L);
        assertThat(exception.getViolations())
            .containsEntry("name", Arrays.asList("Invalid name"));
    }

    @Test
    void shouldPreferFieldErrorsOverViolations() {
        RoleValidationException exception = RoleValidationExceptionBuilder.builder()
            .violation("name", "Violation message")
            .fieldError("name", "Field error message")
            .build();

        assertThat(exception.getViolations())
            .containsEntry("name", Arrays.asList("Field error message"))
            .doesNotContainEntry("name", Arrays.asList("Violation message"));
    }

    @Test
    void shouldHandleNullFieldValues() {
        RoleValidationException exception = RoleValidationExceptionBuilder.builder()
            .message(null)
            .roleId(null)
            .build();

        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getRoleId()).isNull();
        assertThat(exception.getViolations()).isEmpty();
    }

    @Test
    void shouldHandleEmptyViolationMessages() {
        RoleValidationException exception = RoleValidationExceptionBuilder.builder()
            .violation("field", "")
            .violation("field", null)
            .build();

        assertThat(exception.getViolations())
            .containsEntry("field", Arrays.asList("", null));
    }

    @Test
    void shouldHandleNullFieldErrorObject() {
        assertThrows(IllegalArgumentException.class, () -> {
            RoleValidationExceptionBuilder.builder()
                .fieldError(null, "message")
                .build();
        });
    }

    @Test
    void shouldCreateImmutableViolationsMap() {
        Map<String, List<String>> violations = new HashMap<>();
        violations.put("field", Arrays.asList("Initial error"));
        
        RoleValidationException exception = RoleValidationExceptionBuilder.builder()
            .violations(violations)
            .build();
        
        // Modify original map
        violations.put("field", Arrays.asList("Modified error"));
        
        // Exception's violations should remain unchanged
        assertThat(exception.getViolations())
            .containsEntry("field", Arrays.asList("Initial error"));
    }

    @Test
    void shouldPreserveFieldErrorMetadata() {
        FieldError fieldError = new FieldError("role", "name", "defaultValue", 
            false, new String[]{"code"}, new Object[]{"arg"}, "Name is invalid");
        
        RoleValidationException exception = RoleValidationExceptionBuilder.builder()
            .fieldError("name", fieldError.getDefaultMessage())
            .build();

        assertThat(exception.getViolations())
            .containsEntry("name", Arrays.asList("Name is invalid"));
    }
}
