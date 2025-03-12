package vn.com.fecredit.app.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

class RoleValidationExceptionTest {

    @Test
    void createBasicException() {
        RoleValidationException exception = new RoleValidationException("Test message");
        
        assertThat(exception.getMessage()).isEqualTo("Test message");
        assertThat(exception.getRoleId()).isNull();
        assertThat(exception.getViolations()).isEmpty();
    }

    @Test
    void createExceptionWithRoleId() {
        Long roleId = 123L;
        RoleValidationException exception = new RoleValidationException("Test message", roleId);
        
        assertThat(exception.getMessage()).isEqualTo("Test message");
        assertThat(exception.getRoleId()).isEqualTo(roleId);
        assertThat(exception.getViolations()).isEmpty();
    }

    @Test
    void createExceptionWithViolations() {
        Map<String, List<String>> violations = new HashMap<>();
        violations.put("name", Arrays.asList("Cannot be empty"));
        
        RoleValidationException exception = new RoleValidationException("Test message", violations);
        
        assertThat(exception.getMessage()).isEqualTo("Test message");
        assertThat(exception.getRoleId()).isNull();
        assertThat(exception.getViolations())
            .containsEntry("name", Arrays.asList("Cannot be empty"));
    }

    @Test
    void createFromBindingResult() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("role", "name", "Name is required");
        
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));
        
        RoleValidationException exception = RoleValidationException.fromBindingResult(bindingResult);
        
        assertThat(exception.getMessage()).isEqualTo("Role validation failed");
        assertThat(exception.getViolations())
            .containsEntry("name", Arrays.asList("Name is required"));
    }

    @Test
    void addViolation() {
        RoleValidationException exception = new RoleValidationException("Test message");
        
        exception.addViolation("field1", "Error 1")
                .addViolation("field1", "Error 2")
                .addViolation("field2", "Error 3");
        
        assertThat(exception.getViolations())
            .containsEntry("field1", Arrays.asList("Error 1", "Error 2"))
            .containsEntry("field2", Arrays.asList("Error 3"));
    }

    @Test
    void checkHasViolations() {
        RoleValidationException exception = new RoleValidationException("Test message");
        
        assertThat(exception.hasViolations()).isFalse();
        
        exception.addViolation("field", "error");
        
        assertThat(exception.hasViolations()).isTrue();
    }

    @Test
    void countViolations() {
        RoleValidationException exception = new RoleValidationException("Test message");
        
        exception.addViolation("field1", "Error 1")
                .addViolation("field1", "Error 2")
                .addViolation("field2", "Error 3");
        
        assertThat(exception.getViolationCount()).isEqualTo(3);
    }

    @Test
    void staticFactoryMethods() {
        RoleValidationException exception1 = RoleValidationException.create("Test message");
        assertThat(exception1.getMessage()).isEqualTo("Test message");
        
        RoleValidationException exception2 = RoleValidationException.forRole(123L, "Test message");
        assertThat(exception2.getMessage()).isEqualTo("Test message");
        assertThat(exception2.getRoleId()).isEqualTo(123L);
    }
}
