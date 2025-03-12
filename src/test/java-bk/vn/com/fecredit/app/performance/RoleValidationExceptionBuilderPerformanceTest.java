package vn.com.fecredit.app.performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import vn.com.fecredit.app.exception.RoleValidationException;
import vn.com.fecredit.app.util.RoleValidationExceptionBuilder;

@Tag("performance")
class RoleValidationExceptionBuilderPerformanceTest {
    private static final int WARM_UP_ITERATIONS = 1000;
    private static final int PERFORMANCE_ITERATIONS = 10_000;
    private static final int CONCURRENT_THREADS = 10;
    
    @BeforeEach
    void warmUp() {
        // Warm up JVM
        IntStream.range(0, WARM_UP_ITERATIONS).forEach(i -> {
            RoleValidationExceptionBuilder.builder()
                .message("Warm up " + i)
                .violation("field" + i, "error")
                .build();
        });
    }

    @Test
    void shouldHandleLargeNumberOfViolations() {
        long startTime = System.nanoTime();
        
        RoleValidationExceptionBuilder builder = RoleValidationExceptionBuilder.builder()
            .message("Large violation test");
            
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            builder.violation("field" + i, "Error message " + i);
        }
        
        RoleValidationException exception = builder.build();
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        
        assertThat(exception.getViolations()).hasSize(PERFORMANCE_ITERATIONS);
        assertThat(duration).isLessThan(1000); // Should complete within 1 second
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 1000, 10000})
    void shouldScaleWithDifferentViolationSizes(int size) {
        Map<String, List<String>> violations = new HashMap<>();
        for (int i = 0; i < size; i++) {
            violations.computeIfAbsent("field" + i, k -> new ArrayList<>())
                     .add("Error " + i);
        }

        long startTime = System.nanoTime();
        RoleValidationException exception = RoleValidationExceptionBuilder.builder()
            .message("Scaling test")
            .violations(violations)
            .build();
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        assertThat(exception.getViolations()).hasSize(size);
        assertThat(duration).isLessThan(size); // Should scale roughly linearly
    }

    @Test
    void shouldHandleConcurrentBuilding() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        Map<Integer, RoleValidationException> results = new ConcurrentHashMap<>();
        
        List<CompletableFuture<Void>> futures = IntStream.range(0, PERFORMANCE_ITERATIONS)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                RoleValidationException exception = RoleValidationExceptionBuilder.builder()
                    .message("Concurrent test " + i)
                    .roleId((long) i)
                    .violation("field", "Thread " + i)
                    .build();
                results.put(i, exception);
            }, executor))
            .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
        
        assertThat(results).hasSize(PERFORMANCE_ITERATIONS);
        assertThat(results.values())
            .extracting(RoleValidationException::getMessage)
            .allMatch(msg -> msg.startsWith("Concurrent test"));
    }

    @Test
    void shouldPerformWellWithMixedOperations() {
        long startTime = System.nanoTime();
        List<RoleValidationException> exceptions = new ArrayList<>();

        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            RoleValidationExceptionBuilder builder = RoleValidationExceptionBuilder.builder()
                .message("Mixed test " + i);

            // Add varying numbers of violations
            int violationCount = i % 10;
            for (int j = 0; j < violationCount; j++) {
                builder.violation("field" + j, "Error " + j);
            }

            // Add field errors for even numbers
            if (i % 2 == 0) {
                builder.fieldError("evenField", "Even numbered error");
            }

            // Add role ID for every third iteration
            if (i % 3 == 0) {
                builder.roleId((long) i);
            }

            exceptions.add(builder.build());
        }

        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        
        assertThat(exceptions).hasSize(PERFORMANCE_ITERATIONS);
        assertThat(duration).isLessThan(2000); // Should complete within 2 seconds
    }

    @Test
    void shouldHandleMemoryEfficiently() {
        List<RoleValidationException> exceptions = new ArrayList<>();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            exceptions.add(RoleValidationExceptionBuilder.builder()
                .message("Memory test " + i)
                .violation("field1", "Error 1")
                .violation("field2", "Error 2")
                .roleId((long) i)
                .build());
        }

        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryPerException = (memoryAfter - memoryBefore) / PERFORMANCE_ITERATIONS;

        assertThat(memoryPerException).isLessThan(1000); // Should use less than 1KB per exception
    }
}
