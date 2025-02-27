package vn.com.fecredit.app.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpinRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldBuildWithDefaultValues() {
        SpinRequest request = SpinRequest.builder()
                .eventId(1L)
                .participantId(2L)
                .build();
        
        assertEquals(1L, request.getEventId());
        assertEquals(2L, request.getParticipantId());
        assertEquals("DEFAULT", request.getLocation());
        assertFalse(request.getHasActiveParticipation());
        assertFalse(request.getIsGoldenHourEligible());
    }

    @Test
    void shouldBuildWithAllValues() {
        SpinRequest request = SpinRequest.builder()
                .eventId(1L)
                .participantId(2L)
                .location("TEST")
                .hasActiveParticipation(true)
                .isGoldenHourEligible(true)
                .build();
        
        assertEquals(1L, request.getEventId());
        assertEquals(2L, request.getParticipantId());
        assertEquals("TEST", request.getLocation());
        assertTrue(request.getHasActiveParticipation());
        assertTrue(request.getIsGoldenHourEligible());
    }

    @Test
    void shouldFailValidationWithNoEventId() {
        SpinRequest request = SpinRequest.builder()
                .participantId(2L)
                .build();
        
        Set<ConstraintViolation<SpinRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        
        ConstraintViolation<SpinRequest> violation = violations.iterator().next();
        assertEquals("eventId", violation.getPropertyPath().toString());
        assertEquals("Event ID is required", violation.getMessage());
    }

    @Test
    void shouldFailValidationWithNoParticipantId() {
        SpinRequest request = SpinRequest.builder()
                .eventId(1L)
                .build();
        
        Set<ConstraintViolation<SpinRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        
        ConstraintViolation<SpinRequest> violation = violations.iterator().next();
        assertEquals("participantId", violation.getPropertyPath().toString());
        assertEquals("Participant ID is required", violation.getMessage());
    }

    @Test
    void shouldValidateAllRequiredFields() {
        SpinRequest request = new SpinRequest(); // All fields null
        
        Set<ConstraintViolation<SpinRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
        
        assertTrue(violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .anyMatch(path -> path.equals("eventId")));
        assertTrue(violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .anyMatch(path -> path.equals("participantId")));
    }

    @Test
    void shouldPassValidationWithRequiredFields() {
        SpinRequest request = SpinRequest.builder()
                .eventId(1L)
                .participantId(2L)
                .build();
        
        Set<ConstraintViolation<SpinRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldHandleNullOptionalValues() {
        SpinRequest request = SpinRequest.builder()
                .eventId(1L)
                .participantId(2L)
                .location(null)
                .hasActiveParticipation(null)
                .isGoldenHourEligible(null)
                .build();
        
        assertEquals("DEFAULT", request.getLocation());
        assertFalse(request.getHasActiveParticipation());
        assertFalse(request.getIsGoldenHourEligible());
        
        // Validate should still pass
        Set<ConstraintViolation<SpinRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldAllowCopyingWithBuilder() {
        SpinRequest original = SpinRequest.builder()
                .eventId(1L)
                .participantId(2L)
                .location("TEST")
                .hasActiveParticipation(true)
                .isGoldenHourEligible(true)
                .build();

        SpinRequest copy = original.toBuilder()
                .location("NEW_LOCATION")
                .hasActiveParticipation(false)
                .build();

        // Verify modified fields
        assertEquals("NEW_LOCATION", copy.getLocation());
        assertFalse(copy.getHasActiveParticipation());

        // Verify copied fields
        assertEquals(1L, copy.getEventId());
        assertEquals(2L, copy.getParticipantId());
        assertTrue(copy.getIsGoldenHourEligible());
        
        // Validate both objects
        assertTrue(validator.validate(original).isEmpty());
        assertTrue(validator.validate(copy).isEmpty());
    }

    @Test
    void shouldHandleSettersAndGetters() {
        SpinRequest request = new SpinRequest();
        
        request.setEventId(1L);
        request.setParticipantId(2L);
        request.setLocation("TEST");
        request.setHasActiveParticipation(true);
        request.setIsGoldenHourEligible(true);

        assertEquals(1L, request.getEventId());
        assertEquals(2L, request.getParticipantId());
        assertEquals("TEST", request.getLocation());
        assertTrue(request.getHasActiveParticipation());
        assertTrue(request.getIsGoldenHourEligible());
        
        // Validate after setting all fields
        assertTrue(validator.validate(request).isEmpty());
    }
}