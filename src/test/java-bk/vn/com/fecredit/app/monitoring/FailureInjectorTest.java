package vn.com.fecredit.app.monitoring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FailureInjector functionality
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FailureInjectorTest {

    @TempDir
    Path tempDir;
    private FailureInjector injector;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        injector = new FailureInjector(0.5); // 50% failure rate
        executor = Executors.newFixedThreadPool(4);
    }

    @Test
    @Order(1)
    @DisplayName("Should inject failures at expected rate")
    void shouldInjectFailuresAtExpectedRate() {
        int attempts = 1000;
        int failures = 0;

        for (int i = 0; i < attempts; i++) {
            try {
                injector.executeWithFailures(() -> "test");
            } catch (Throwable e) {
                failures++;
            }
        }

        double actualRate = (double) failures / attempts;
        assertTrue(Math.abs(actualRate - 0.5) < 0.1,
            String.format("Failure rate should be close to 0.5, was: %.2f", actualRate));
    }

    @Test
    @Order(2)
    @DisplayName("Should corrupt file content")
    void shouldCorruptFileContent() throws IOException {
        // Create test file
        Path testFile = tempDir.resolve("test.txt");
        String originalContent = "Test content for corruption";
        Files.writeString(testFile, originalContent);

        // Corrupt file
        injector.corruptFile(testFile);

        // Verify content was modified
        String corruptedContent = Files.readString(testFile);
        assertNotEquals(originalContent, corruptedContent,
            "File content should be corrupted");
    }

    @Test
    @Order(3)
    @DisplayName("Should handle system-level failures")
    void shouldHandleSystemFailures() {
        int attempts = 100;
        int successfulAttempts = 0;

        for (int i = 0; i < attempts; i++) {
            try {
                injector.injectSystemFailure();
                successfulAttempts++;
            } catch (Throwable e) {
                // Expected failures
            }
        }

        assertTrue(successfulAttempts > 0,
            "Some system failure injections should succeed");
    }

    @Test
    @Order(4)
    @DisplayName("Should corrupt data structures")
    void shouldCorruptDataStructures() {
        byte[] testData = new byte[100];
        byte[] original = testData.clone();

        injector.corruptData(testData);

        assertFalse(java.util.Arrays.equals(original, testData),
            "Data should be corrupted");
    }

    @Test
    @Order(5)
    @DisplayName("Should handle concurrent failure injection")
    void shouldHandleConcurrentFailures() throws Exception {
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        ConcurrentHashMap<String, Integer> results = new ConcurrentHashMap<>();
        results.put("success", 0);
        results.put("failure", 0);

        // Create concurrent tasks
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < 100; j++) {
                        try {
                            injector.executeWithFailures(() -> "test");
                            results.computeIfPresent("success", (k, v) -> v + 1);
                        } catch (Throwable e) {
                            results.computeIfPresent("failure", (k, v) -> v + 1);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();
        completionLatch.await(30, TimeUnit.SECONDS);

        int totalOperations = results.get("success") + results.get("failure");
        double failureRate = (double) results.get("failure") / totalOperations;

        assertTrue(Math.abs(failureRate - 0.5) < 0.1,
            String.format("Concurrent failure rate should be close to 0.5, was: %.2f", failureRate));
    }

    @Test
    @Order(6)
    @DisplayName("Should exhaust resources")
    void shouldExhaustResources() {
        int attempts = 10;
        boolean resourceExhausted = false;

        for (int i = 0; i < attempts && !resourceExhausted; i++) {
            try {
                injector.exhaustResources();
            } catch (OutOfMemoryError | IOException e) {
                resourceExhausted = true;
            } catch (Throwable ignored) {}
        }

        assertTrue(resourceExhausted,
            "Should eventually exhaust system resources");
    }

    @Test
    @Order(7)
    @DisplayName("Should handle network failures")
    void shouldHandleNetworkFailures() {
        int attempts = 100;
        int failures = 0;

        for (int i = 0; i < attempts; i++) {
            try {
                injector.injectNetworkFailure();
            } catch (IOException e) {
                failures++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        assertTrue(failures > 0,
            "Should inject some network failures");
    }

    @ParameterizedTest
    @Order(8)
    @ValueSource(doubles = {0.0, 0.25, 0.5, 0.75, 1.0})
    @DisplayName("Should respect different failure rates")
    void shouldRespectFailureRates(double rate) {
        FailureInjector rateInjector = new FailureInjector(rate);
        int attempts = 1000;
        int failures = 0;

        for (int i = 0; i < attempts; i++) {
            try {
                rateInjector.executeWithFailures(() -> null);
            } catch (Throwable e) {
                failures++;
            }
        }

        double actualRate = (double) failures / attempts;
        assertTrue(Math.abs(actualRate - rate) < 0.1,
            String.format("Failure rate should be close to %.2f, was: %.2f", rate, actualRate));
    }

    @Test
    @Order(9)
    @DisplayName("Should track injected failures")
    void shouldTrackInjectedFailures() {
        int attempts = 100;
        int countedFailures = 0;

        for (int i = 0; i < attempts; i++) {
            try {
                injector.executeWithFailures(() -> "test");
            } catch (Throwable e) {
                countedFailures++;
            }
        }

        assertEquals(countedFailures, injector.getInjectedFailures(),
            "Should accurately track number of injected failures");
    }

    @AfterEach
    void cleanup() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
