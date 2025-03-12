package vn.com.fecredit.app.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.validation.ValidationUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("performance")
class ValidationTestHelperPerformanceTest {

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
    void shouldHandleLargeNumberOfViolationsEfficiently() {
        // Given
        int violationCount = 10_000;
        Set<ConstraintViolation<Participant>> violations = createLargeViolationSet(violationCount);
        when(validationUtils.validate(any(Participant.class))).thenReturn(violations);

        // When
        long startTime = System.nanoTime();
        Map<String, Set<String>> allViolations = helper.getAllViolations(participant);
        long endTime = System.nanoTime();

        // Then
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        assertThat(durationMs).isLessThan(1000); // Should process in less than 1 second
        assertThat(allViolations).hasSize(violationCount / 2); // Due to field name alternation
    }

    @Test
    void shouldPerformWellWithMultipleViolationsPerField() {
        // Given
        int violationsPerField = 1000;
        Set<ConstraintViolation<Participant>> violations = createViolationsForField("email", violationsPerField);
        when(validationUtils.validate(any(Participant.class))).thenReturn(violations);

        // When
        long startTime = System.nanoTime();
        Map<String, Set<String>> allViolations = helper.getAllViolations(participant);
        long endTime = System.nanoTime();

        // Then
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        assertThat(durationMs).isLessThan(500); // Should process in less than 500ms
        assertThat(allViolations.get("email")).hasSize(violationsPerField);
    }

    @Test
    void shouldHandleConcurrentValidationEfficiently() {
        // Given
        int threadCount = 10;
        int violationsPerThread = 1000;
        Set<ConstraintViolation<Participant>> violations = createViolationsForField("email", violationsPerThread);
        when(validationUtils.validate(any(Participant.class))).thenReturn(violations);

        // When
        long startTime = System.nanoTime();
        List<Map<String, Set<String>>> results = IntStream.range(0, threadCount)
            .parallel()
            .mapToObj(i -> helper.getAllViolations(new Participant()))
            .collect(Collectors.toList());
        long endTime = System.nanoTime();

        // Then
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        assertThat(durationMs).isLessThan(2000); // Should process all threads in less than 2 seconds
        assertThat(results).hasSize(threadCount);
        results.forEach(result -> assertThat(result.get("email")).hasSize(violationsPerThread));
    }

    @Test
    void shouldPerformWellWithMessageSearching() {
        // Given
        int violationCount = 10_000;
        Set<ConstraintViolation<Participant>> violations = createViolationsWithPattern(violationCount);
        when(validationUtils.validate(any(Participant.class))).thenReturn(violations);

        // When
        long startTime = System.nanoTime();
        Set<String> matchingViolations = helper.getViolationsContaining(participant, "pattern-500");
        long endTime = System.nanoTime();

        // Then
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        assertThat(durationMs).isLessThan(500); // Should search in less than 500ms
        assertThat(matchingViolations).hasSize(1);
    }

    @Test
    void shouldHandleGroupValidationPerformance() {
        // Given
        int groupCount = 100;
        int violationsPerGroup = 100;
        Class<?>[] groups = IntStream.range(0, groupCount)
            .mapToObj(i -> TestGroup.class)
            .toArray(Class<?>[]::new);
        Set<ConstraintViolation<Participant>> violations = createViolationsForField("field", violationsPerGroup);
        when(validationUtils.validateForGroups(any(Participant.class), any())).thenReturn(violations);

        // When
        long startTime = System.nanoTime();
        Map<String, String> groupViolations = helper.validateGroups(participant, groups);
        long endTime = System.nanoTime();

        // Then
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        assertThat(durationMs).isLessThan(500); // Should validate groups in less than 500ms
        assertThat(groupViolations).isNotEmpty();
    }

    private Set<ConstraintViolation<Participant>> createLargeViolationSet(int count) {
        Set<ConstraintViolation<Participant>> violations = new HashSet<>();
        for (int i = 0; i < count; i++) {
            violations.add(createViolation(
                "field" + (i % 2), // Alternate between two fields
                "Violation message " + i
            ));
        }
        return violations;
    }

    private Set<ConstraintViolation<Participant>> createViolationsForField(String field, int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createViolation(field, "Violation " + i))
            .collect(Collectors.toSet());
    }

    private Set<ConstraintViolation<Participant>> createViolationsWithPattern(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createViolation("field", "pattern-" + i))
            .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private ConstraintViolation<Participant> createViolation(String path, String message) {
        ConstraintViolation<Participant> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn(path);
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    private interface TestGroup {}
}
