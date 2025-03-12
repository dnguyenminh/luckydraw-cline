package vn.com.fecredit.app.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import vn.com.fecredit.app.exception.RoleValidationException;
import vn.com.fecredit.app.util.RoleValidationExceptionBuilder;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class RoleValidationExceptionBuilderBenchmark {

    @Param({"10", "100", "1000"})
    private int violationCount;

    private Map<String, List<String>> violations;
    private List<String> fields;
    private List<String> messages;

    @Setup(Level.Trial)
    public void setup() {
        violations = new HashMap<>();
        fields = new ArrayList<>();
        messages = new ArrayList<>();

        for (int i = 0; i < violationCount; i++) {
            fields.add("field" + i);
            messages.add("error" + i);
            violations.computeIfAbsent("field" + i, k -> new ArrayList<>())
                     .add("error" + i);
        }
    }

    @Benchmark
    public RoleValidationException buildBasicException() {
        return RoleValidationExceptionBuilder.builder()
            .message("Basic benchmark test")
            .build();
    }

    @Benchmark
    public RoleValidationException buildWithRoleId() {
        return RoleValidationExceptionBuilder.builder()
            .message("Role ID benchmark test")
            .roleId(123L)
            .build();
    }

    @Benchmark
    public RoleValidationException buildWithSingleViolation() {
        return RoleValidationExceptionBuilder.builder()
            .message("Single violation benchmark test")
            .violation("field", "error")
            .build();
    }

    @Benchmark
    public RoleValidationException buildWithMultipleViolations() {
        RoleValidationExceptionBuilder builder = RoleValidationExceptionBuilder.builder()
            .message("Multiple violations benchmark test");

        for (int i = 0; i < fields.size(); i++) {
            builder.violation(fields.get(i), messages.get(i));
        }

        return builder.build();
    }

    @Benchmark
    public RoleValidationException buildWithBulkViolations() {
        return RoleValidationExceptionBuilder.builder()
            .message("Bulk violations benchmark test")
            .violations(violations)
            .build();
    }

    @Benchmark
    public RoleValidationException buildWithFieldErrors() {
        RoleValidationExceptionBuilder builder = RoleValidationExceptionBuilder.builder()
            .message("Field errors benchmark test");

        for (int i = 0; i < fields.size(); i++) {
            builder.fieldError(fields.get(i), messages.get(i));
        }

        return builder.build();
    }

    @Benchmark
    public RoleValidationException buildComplexException() {
        RoleValidationExceptionBuilder builder = RoleValidationExceptionBuilder.builder()
            .message("Complex benchmark test")
            .roleId(123L);

        // Add individual violations
        for (int i = 0; i < fields.size() / 2; i++) {
            builder.violation(fields.get(i), messages.get(i));
        }

        // Add bulk violations
        Map<String, List<String>> bulkViolations = new HashMap<>();
        for (int i = fields.size() / 2; i < fields.size(); i++) {
            bulkViolations.computeIfAbsent(fields.get(i), k -> new ArrayList<>())
                         .add(messages.get(i));
        }
        builder.violations(bulkViolations);

        // Add field errors
        for (int i = 0; i < 3; i++) {
            builder.fieldError("fieldError" + i, "Field error " + i);
        }

        return builder.build();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(RoleValidationExceptionBuilderBenchmark.class.getSimpleName())
            .build();
        new Runner(opt).run();
    }
}
