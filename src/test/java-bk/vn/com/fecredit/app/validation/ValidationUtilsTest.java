package vn.com.fecredit.app.validation;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import vn.com.fecredit.app.entity.Participant;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationUtilsTest {

    @Mock
    private MessageSource messageSource;

    private ValidationUtils validationUtils;
    private Participant participant;

    @BeforeEach
    void setUp() {
        validationUtils = new ValidationUtils(messageSource);
        participant = new Participant();
        participant.setStatus(EntityStatus.ACTIVE);
    }

    @Test
    void shouldValidateValidParticipant() {
        // Given
        setupValidParticipant();

        // When
        Set<ConstraintViolation<Participant>> violations = validationUtils.validate(participant);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldValidateInvalidParticipant() {
        // Given invalid participant (missing required fields)
        participant.setFullName("");
        participant.setPhoneNumber("");

        // When
        Set<ConstraintViolation<Participant>> violations = validationUtils.validate(participant);

        // Then
        assertThat(violations).hasSize(2);
        assertThat(violations)
            .extracting(v -> v.getPropertyPath().toString())
            .containsOnly("fullName", "phoneNumber");
    }

    @Test
    void shouldValidateAndGetMessages() {
        // Given
        participant.setFullName("");
        when(messageSource.getMessage(eq("participant.fullName.required"), any(), any(Locale.class)))
            .thenReturn("Full name is required");

        // When
        Map<String, String> errors = validationUtils.validateAndGetMessages(participant);

        // Then
        assertThat(errors).containsKey("fullName");
        assertThat(errors.get("fullName")).contains("required");
    }

    @Test
    void shouldValidateSpecificProperty() {
        // Given
        participant.setEmail("invalid-email");

        // When
        Set<ConstraintViolation<Participant>> violations = validationUtils.validateProperty(participant, "email");

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("valid");
    }

    @Test
    void shouldValidateForGroups() {
        // Given
        setupValidParticipant();
        participant.setDailySpinLimit(100);
        participant.setTotalSpinLimit(50);

        // When
        Set<ConstraintViolation<Participant>> violations = validationUtils.validateForGroups(participant);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("spin limit");
    }

    @Test
    void shouldCreateValidationError() {
        // Given
        String expectedMessage = "Test error message";
        when(messageSource.getMessage(eq("test.error"), any(), any(Locale.class)))
            .thenReturn(expectedMessage);

        // When
        Map<String, String> error = validationUtils.createValidationError("field", "test.error");

        // Then
        assertThat(error).containsEntry("field", expectedMessage);
    }

    @Test
    void shouldInterpolateMessage() {
        // Given
        String messageKey = "test.message";
        String expectedMessage = "Test message with value: 42";
        when(messageSource.getMessage(eq(messageKey), any(), any(Locale.class)))
            .thenReturn(expectedMessage);

        // When
        String result = validationUtils.interpolateMessage("{" + messageKey + "}", Map.of("value", 42));

        // Then
        assertThat(result).isEqualTo(expectedMessage);
    }

    private void setupValidParticipant() {
        participant.setFullName("John Doe");
        participant.setEmail("john.doe@example.com");
        participant.setPhoneNumber("0123456789");
        participant.setCustomerId("CUST123");
        participant.setCardNumber("4111111111111111");
        participant.setProvince("Hanoi");
        participant.setDailySpinLimit(10);
        participant.setTotalSpinLimit(100);
        participant.setDeviceId("device123");
        participant.setSessionId("session123");
    }
}
