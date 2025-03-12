package vn.com.fecredit.app.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.com.fecredit.app.dto.ResetPasswordRequest;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.security.JwtService;
import vn.com.fecredit.app.service.TokenBlacklistService;
import vn.com.fecredit.app.service.UserService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityStressTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    private User testUser;
    private ExecutorService executorService;
    private ScheduledExecutorService monitoringExecutor;
    private MetricsCollector metricsCollector;

    @BeforeEach
    void setUp() {
        testUser = (User) userService.loadUserByUsername("testuser");
        executorService = Executors.newFixedThreadPool(100);
        monitoringExecutor = Executors.newSingleThreadScheduledExecutor();
        metricsCollector = new MetricsCollector();
    }

    @Test
    void loginEndpoint_UnderStress() throws Exception {
        // Given
        int durationSeconds = 60;
        int maxConcurrentUsers = 100;
        AtomicInteger activeUsers = new AtomicInteger(0);
        AtomicInteger successfulRequests = new AtomicInteger(0);
        AtomicInteger failedRequests = new AtomicInteger(0);

        CountDownLatch completionLatch = new CountDownLatch(1);
        Semaphore concurrencyLimiter = new Semaphore(maxConcurrentUsers);

        // Start metrics monitoring
        ScheduledFuture<?> monitoringTask = startMonitoring(
            activeUsers, successfulRequests, failedRequests);

        try {
            // When - Run stress test for specified duration
            Instant startTime = Instant.now();
            Instant endTime = startTime.plusSeconds(durationSeconds);

            while (Instant.now().isBefore(endTime)) {
                if (concurrencyLimiter.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                    executorService.submit(() -> {
                        try {
                            activeUsers.incrementAndGet();
                            performLoginRequest();
                            successfulRequests.incrementAndGet();
                        } catch (Exception e) {
                            failedRequests.incrementAndGet();
                        } finally {
                            activeUsers.decrementAndGet();
                            concurrencyLimiter.release();
                        }
                    });
                }
            }

            // Wait for remaining tasks to complete
            completionLatch.await(10, TimeUnit.SECONDS);

            // Then
            MetricsCollector.Metrics finalMetrics = metricsCollector.getMetrics();
            System.out.println("Stress Test Results:");
            System.out.println("Total Requests: " + (successfulRequests.get() + failedRequests.get()));
            System.out.println("Successful Requests: " + successfulRequests.get());
            System.out.println("Failed Requests: " + failedRequests.get());
            System.out.println("Average Response Time: " + finalMetrics.getAverageResponseTime() + "ms");
            System.out.println("Max Response Time: " + finalMetrics.getMaxResponseTime() + "ms");
            System.out.println("Error Rate: " + (failedRequests.get() * 100.0 / (successfulRequests.get() + failedRequests.get())) + "%");

            assertThat(failedRequests.get())
                .as("Failed requests should be less than 1% of total requests")
                .isLessThan((successfulRequests.get() + failedRequests.get()) / 100);
            assertThat(finalMetrics.getAverageResponseTime())
                .as("Average response time should be under 500ms under stress")
                .isLessThan(500.0);
        } finally {
            monitoringTask.cancel(true);
        }
    }

    @Test
    void tokenBlacklist_UnderStress() throws Exception {
        // Given
        int totalTokens = 10000;
        int maxConcurrentOperations = 100;
        AtomicInteger completedOperations = new AtomicInteger(0);
        AtomicInteger failedOperations = new AtomicInteger(0);
        List<String> tokens = generateTokens(totalTokens);

        Semaphore concurrencyLimiter = new Semaphore(maxConcurrentOperations);
        CountDownLatch completionLatch = new CountDownLatch(totalTokens);

        // When
        Instant startTime = Instant.now();

        tokens.parallelStream().forEach(token -> {
            try {
                concurrencyLimiter.acquire();
                executorService.submit(() -> {
                    try {
                        tokenBlacklistService.blacklist(
                            token, 
                            false,
                            System.currentTimeMillis() + 3600000,
                            "testuser",
                            "stress_test"
                        );
                        completedOperations.incrementAndGet();
                    } catch (Exception e) {
                        failedOperations.incrementAndGet();
                    } finally {
                        concurrencyLimiter.release();
                        completionLatch.countDown();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                failedOperations.incrementAndGet();
                completionLatch.countDown();
            }
        });

        // Wait for completion or timeout
        boolean completed = completionLatch.await(5, TimeUnit.MINUTES);
        Duration duration = Duration.between(startTime, Instant.now());

        // Then
        System.out.println("Token Blacklist Stress Test Results:");
        System.out.println("Total Operations: " + totalTokens);
        System.out.println("Completed Operations: " + completedOperations.get());
        System.out.println("Failed Operations: " + failedOperations.get());
        System.out.println("Total Duration: " + duration.toMillis() + "ms");
        System.out.println("Operations per Second: " + 
                (completedOperations.get() * 1000.0 / duration.toMillis()));

        assertThat(completed).isTrue();
        assertThat(failedOperations.get())
            .as("Failed operations should be less than 1% of total operations")
            .isLessThan(totalTokens / 100);
    }

    private List<String> generateTokens(int count) {
        List<String> tokens = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            tokens.add(jwtService.generateToken(testUser));
        }
        return tokens;
    }

    private ScheduledFuture<?> startMonitoring(
            AtomicInteger activeUsers,
            AtomicInteger successfulRequests,
            AtomicInteger failedRequests) {
        return monitoringExecutor.scheduleAtFixedRate(() -> {
            MetricsCollector.Metrics currentMetrics = metricsCollector.getMetrics();
            System.out.printf(
                "Active Users: %d, Successful: %d, Failed: %d, Avg Response Time: %.2fms%n",
                activeUsers.get(),
                successfulRequests.get(),
                failedRequests.get(),
                currentMetrics.getAverageResponseTime()
            );
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void performLoginRequest() throws Exception {
        Instant start = Instant.now();
        try {
            ResetPasswordRequest.LoginRequest loginRequest = ResetPasswordRequest.LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(loginRequest)))
                    .andExpect(status().isOk());
        } finally {
            metricsCollector.recordResponseTime(
                Duration.between(start, Instant.now()).toMillis()
            );
        }
    }

    private String toJson(Object obj) throws Exception {
        return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
    }

    private static class MetricsCollector {
        private final Queue<Long> responseTimes = new ConcurrentLinkedQueue<>();
        private static final int MAX_SAMPLES = 1000;

        public void recordResponseTime(long responseTime) {
            responseTimes.offer(responseTime);
            while (responseTimes.size() > MAX_SAMPLES) {
                responseTimes.poll();
            }
        }

        public Metrics getMetrics() {
            List<Long> times = new ArrayList<>(responseTimes);
            if (times.isEmpty()) {
                return new Metrics(0.0, 0L);
            }

            double average = times.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            long max = times.stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0L);

            return new Metrics(average, max);
        }

        public static class Metrics {
            private final double averageResponseTime;
            private final long maxResponseTime;

            public Metrics(double averageResponseTime, long maxResponseTime) {
                this.averageResponseTime = averageResponseTime;
                this.maxResponseTime = maxResponseTime;
            }

            public double getAverageResponseTime() {
                return averageResponseTime;
            }

            public long getMaxResponseTime() {
                return maxResponseTime;
            }
        }
    }
}
