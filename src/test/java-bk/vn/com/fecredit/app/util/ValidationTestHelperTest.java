package vn.com.fecredit.app.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.validation.ValidationUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationTestHelperTest {

    @Mock
    private ValidationUtils validationUtils;

    private ValidationTestHelper helper;
    private Participant participant;

    @BeforeEach
    void setUp() {
        helper = new ValidationTestHelper(validationUtils);
        participant = new Participant();
    }

    @Test
    void shouldReturnTrueWhenNoViolations() {
        // Given
        when(validationUtils.validate(participant)).thenReturn(Collections.emptySet());

        // When
        boolean isValid = helper.isValid(participant);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseWhenViolationsExist() {
        // Given
        Set<ConstraintViolation<Participant>> violations = createViolationSet("email", "Invalid email format");
        when(validationUtils.validate(participant)).thenReturn(violations);

        // When
        boolean isValid = helper.isValid(participant);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldValidateSpecificField() {
        // Given
        Set<ConstraintViolation<Participant>> violations = createViolationSet("phoneNumber", "Invalid phone number");
        when(validationUtils.validateProperty(any(), any())).thenReturn(violations);

        // When
        Set<String> messages = helper.validateField(participant, "phoneNumber");

        // Then
        assertThat(messages).containsExactly("Invalid phone number");
    }

    @Test
    void shouldHandleNullInput() {
        // Given
        when(validationUtils.validate(null)).thenReturn(Collections.emptySet());

        // When
        boolean isValid = helper.isValid(null);

        // Then
        assertThat(isValid).isTrue();
    }

    private Set<ConstraintViolation<Participant>> createViolationSet(String propertyPath, String message) {
        Set<ConstraintViolation<Participant>> violations = new HashSet<>();
        violations.add(createViolation(propertyPath, message));
        return violations;
    }

    @SuppressWarnings("unchecked")
    private ConstraintViolation<Participant> createViolation(String propertyPath, String message) {
        ConstraintViolation<Participant> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn(propertyPath);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }
}
