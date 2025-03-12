package vn.com.fecredit.app.benchmark;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.validation.ValidationUtils;
import vn.com.fecredit.app.util.ValidationTestHelper;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
public class ValidationTestHelperBenchmark {

    private ValidationTestHelper helper;
    private ValidationUtils validationUtils;
    private Participant participant;
    private Set<ConstraintViolation<Participant>> largeViolationSet;
    private Set<ConstraintViolation<Participant>> singleFieldViolations;
    private Set<ConstraintViolation<Participant>> searchableViolations;
    private Class<?>[] validationGroups;

    @Setup
    public void setup() {
        validationUtils = mock(ValidationUtils.class);
        helper = new ValidationTestHelper(validationUtils);
        participant = new Participant();

        // Create test data
        largeViolationSet = createLargeViolationSet(10_000);
        singleFieldViolations = createViolationsForField("email", 1000);
        searchableViolations = createViolationsWithPattern(10_000);
        validationGroups = createValidationGroups(100);

        // Setup mock responses
        when(validationUtils.validate(any(Participant.class))).thenReturn(largeViolationSet);
        when(validationUtils.validateForGroups(any(), any())).thenReturn(singleFieldViolations);
    }

    @Benchmark
    public void benchmarkLargeViolationSetProcessing(Blackhole blackhole) {
        blackhole.consume(helper.getAllViolations(participant));
    }

    @Benchmark
    public void benchmarkSingleFieldViolations(Blackhole blackhole) {
        blackhole.consume(helper.validateField(participant, "email"));
    }

    @Benchmark
    public void benchmarkMessageSearching(Blackhole blackhole) {
        when(validationUtils.validate(any())).thenReturn(searchableViolations);
        blackhole.consume(helper.getViolationsContaining(participant, "pattern-500"));
    }

    @Benchmark
    public void benchmarkGroupValidation(Blackhole blackhole) {
        blackhole.consume(helper.validateGroups(participant, validationGroups));
    }

    @Benchmark
    @Threads(10)
    public void benchmarkConcurrentValidation(Blackhole blackhole) {
        blackhole.consume(helper.getAllViolations(participant));
    }

    @Benchmark
    public void benchmarkValidationWithNulls(Blackhole blackhole) {
        when(validationUtils.validate(any())).thenReturn(Collections.emptySet());
        blackhole.consume(helper.getAllViolations(null));
    }

    private Set<ConstraintViolation<Participant>> createLargeViolationSet(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createViolation(
                    "field" + (i % 2),
                    "Violation message " + i))
                .collect(Collectors.toSet());
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

    private Class<?>[] createValidationGroups(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> TestGroup.class)
                .toArray(Class<?>[]::new);
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
