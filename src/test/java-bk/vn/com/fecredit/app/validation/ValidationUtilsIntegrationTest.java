package vn.com.fecredit.app.validation;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ValidationUtilsIntegrationTest {

    @Autowired
    private MessageSource messageSource;

    private ValidationUtils validationUtils;
    private Participant participant;

    @BeforeEach
    void setUp() {
        validationUtils = new ValidationUtils(messageSource);
        participant = new Participant();
        participant.setStatus(EntityStatus.ACTIVE);
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @Test
    void shouldValidateParticipantWithDefaultLocale() {
        // Given invalid participant
        participant.setEmail("invalid-email");
        participant.setPhoneNumber("abc"); // should be digits only

        // When
        Map<String, String> errors = validationUtils.validateAndGetMessages(participant);

        // Then
        assertThat(errors).hasSize(4); // email, phone, fullName, customerId
        assertThat(errors).containsKey("email");
        assertThat(errors).containsKey("phoneNumber");
    }

    @Test
    void shouldValidateParticipantWithVietnameseLocale() {
        // Given
        LocaleContextHolder.setLocale(new Locale("vi"));
        participant.setEmail("invalid-email");

        // When
        Map<String, String> errors = validationUtils.validateAndGetMessages(participant);

        // Then
        assertThat(errors).hasSize(3);
        assertThat(errors).containsKey("email");
    }

    @Test
    void shouldValidateSpinLimitsConsistency() {
        // Given
        setupValidParticipant();
        participant.setDailySpinLimit(100);
        participant.setTotalSpinLimit(50);

        // When
        Set<ConstraintViolation<Participant>> violations = validationUtils.validate(participant);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<Participant> violation = violations.iterator().next();
        assertThat(violation.getMessage()).contains("spin limit");
    }

    @Test
    void shouldValidateComplexObject() {
        // Given
        Event event = new Event();
        participant.setEvent(event);
        participant.setDailySpinLimit(-1);

        // When
        Map<String, String> errors = validationUtils.validateAndGetMessages(participant);

        // Then
        assertThat(errors)
            .containsKey("dailySpinLimit")
            .hasEntrySatisfying("dailySpinLimit", message -> 
                assertThat(message).contains("negative"));
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        participant = null;

        // When
        Set<ConstraintViolation<Participant>> violations = validationUtils.validate(participant);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldValidatePropertyWithDefaultMessage() {
        // Given
        participant.setDeviceId("");

        // When
        Set<ConstraintViolation<Participant>> violations = 
            validationUtils.validateProperty(participant, "deviceId");

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("required");
    }

    @Test
    void shouldValidateAllConstraints() {
        // Given
        setupInvalidParticipant();

        // When
        Map<String, String> errors = validationUtils.validateAndGetMessages(participant);

        // Then
        assertThat(errors)
            .containsKeys(
                "fullName",
                "email",
                "phoneNumber",
                "customerId",
                "deviceId",
                "sessionId"
            );
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

    private void setupInvalidParticipant() {
        participant.setFullName("");
        participant.setEmail("invalid-email");
        participant.setPhoneNumber("abc");
        participant.setCustomerId("");
        participant.setCardNumber("abc");
        participant.setDeviceId("");
        participant.setSessionId("");
        participant.setDailySpinLimit(-1);
        participant.setTotalSpinLimit(-1);
    }
}
