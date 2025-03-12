package vn.com.fecredit.app.performance.trend;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Tag("performance")
class DataPointPerformanceTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final int CONCURRENT_THREADS = 10;
    private static final int ITERATIONS = 100_000;

    @Test
    void shouldBeThreadSafe() throws InterruptedException {
        // Given
        int numThreads = CONCURRENT_THREADS;
        CountDownLatch latch = new CountDownLatch(numThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<DataPoint> points = Collections.synchronizedList(new ArrayList<>());

        // When
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 1000; j++) {
                        points.add(new DataPoint(
                            BASE_TIME.plusSeconds(threadNum * 1000 + j),
                            j,
                            0.0
                        ));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS),
            "All threads should complete within timeout");
        executor.shutdown();
        assertEquals(numThreads * 1000, points.size(),
            "Should create all points without interference");
    }

    @ParameterizedTest
    @ValueSource(ints = {1000, 10000, 100000})
    void shouldMaintainPerformanceWithLargeDatasets(int size) {
        // Given
        List<DataPoint> points = new ArrayList<>(size);
        long startTime = System.nanoTime();

        // When
        for (int i = 0; i < size; i++) {
            points.add(new DataPoint(
                BASE_TIME.plusSeconds(i),
                Math.sin(i * 0.1),
                0.0
            ));
        }

        // Sort to test comparison performance
        Collections.sort(points);

        long duration = System.nanoTime() - startTime;

        // Then
        assertTrue(duration < TimeUnit.SECONDS.toNanos(1),
            String.format("Should process %d points within 1 second, took %.2f ms",
                size, duration / 1_000_000.0));
    }

    @Test
    void shouldBeSerializable() throws IOException, ClassNotFoundException {
        // Given
        DataPoint original = new DataPoint(BASE_TIME, 100.0, 5.0);

        // When
        byte[] serialized;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
            serialized = baos.toByteArray();
        }

        DataPoint deserialized;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserialized = (DataPoint) ois.readObject();
        }

        // Then
        assertAll(
            () -> assertEquals(original.timestamp(), deserialized.timestamp()),
            () -> assertEquals(original.value(), deserialized.value()),
            () -> assertEquals(original.deviation(), deserialized.deviation())
        );
    }

    @Test
    void shouldHandleHighFrequencyOperations() {
        // Given
        DataPoint point = new DataPoint(BASE_TIME, 100.0, 5.0);
        long startTime = System.nanoTime();

        // When
        for (int i = 0; i < ITERATIONS; i++) {
            point.isStable();
            point.relativeChange(i);
            point.absoluteChange(i);
            point.withValue(i);
            point.withDeviation(i % 10);
        }

        long duration = System.nanoTime() - startTime;

        // Then
        double operationsPerSecond = (ITERATIONS * 5.0) / TimeUnit.NANOSECONDS.toSeconds(duration);
        assertTrue(operationsPerSecond > 1_000_000,
            String.format("Should handle at least 1M ops/sec, got %.2f ops/sec",
                operationsPerSecond));
    }

    @Test
    void shouldHandleConcurrentComparisons() throws InterruptedException {
        // Given
        List<DataPoint> points = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            points.add(new DataPoint(BASE_TIME.plusSeconds(i), i, 0.0));
        }

        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);

        // When
        long startTime = System.nanoTime();
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        Collections.sort(new ArrayList<>(points));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS),
            "Concurrent sorting should complete within timeout");
        executor.shutdown();

        long duration = System.nanoTime() - startTime;
        assertTrue(duration < TimeUnit.SECONDS.toNanos(5),
            "Concurrent operations should complete within 5 seconds");
    }

    @Test
    void shouldOptimizeMemoryUsage() {
        // Given
        Runtime runtime = Runtime.getRuntime();
        System.gc(); // Try to clean up before measurement
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        // When
        List<DataPoint> points = new ArrayList<>(ITERATIONS);
        for (int i = 0; i < ITERATIONS; i++) {
            points.add(new DataPoint(BASE_TIME.plusSeconds(i), i, 0.0));
        }

        System.gc(); // Try to clean up temporary objects
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryPerObject = (memoryAfter - memoryBefore) / ITERATIONS;

        // Then
        assertTrue(memoryPerObject < 100,
            String.format("Memory per object should be less than 100 bytes, was %d bytes",
                memoryPerObject));
    }
}
