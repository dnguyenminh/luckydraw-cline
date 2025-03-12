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
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.security.JwtService;
import vn.com.fecredit.app.service.TokenBlacklistService;
import vn.com.fecredit.app.service.UserService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityEnduranceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    private User testUser;
    private ExecutorService userSimulators;
    private ScheduledExecutorService monitoringExecutor;
    private AtomicBoolean testRunning;
    private EnduranceMetrics metrics;

    @BeforeEach
    void setUp() {
        testUser = (User) userService.loadUserByUsername("testuser");
        userSimulators = Executors.newFixedThreadPool(50);
        monitoringExecutor = Executors.newScheduledThreadPool(2);
        testRunning = new AtomicBoolean(true);
        metrics = new EnduranceMetrics();
    }

    @Test
    void systemEndurance_UnderConstantLoad() throws Exception {
        // Given
        int testDurationMinutes = 30; // 30-minute endurance test
        int userCount = 20;
        CountDownLatch testCompletion = new CountDownLatch(1);
        List<Future<?>> userTasks = new ArrayList<>();

        // Start monitoring
        ScheduledFuture<?> monitoringTask = startMonitoring();
        ScheduledFuture<?> cleanupTask = scheduleCleanupTask();

        try {
            // When - Start user simulations
            Instant startTime = Instant.now();
            Instant endTime = startTime.plus(Duration.ofMinutes(testDurationMinutes));

            // Launch user simulators
            for (int i = 0; i < userCount; i++) {
                userTasks.add(userSimulators.submit(new UserSimulator(endTime)));
            }

            // Wait for test duration
            boolean completed = testCompletion.await(testDurationMinutes + 1, TimeUnit.MINUTES);

            // Then
            EnduranceMetrics.Summary summary = metrics.generateSummary();
            System.out.println("\nEndurance Test Summary:");
            System.out.println("======================");
            System.out.println("Test Duration: " + Duration.between(startTime, Instant.now()).toMinutes() + " minutes");
            System.out.println("Total Requests: " + summary.totalRequests);
            System.out.println("Successful Requests: " + summary.successfulRequests);
            System.out.println("Failed Requests: " + summary.failedRequests);
            System.out.println("Average Response Time: " + summary.averageResponseTime + "ms");
            System.out.println("95th Percentile Response Time: " + summary.percentile95 + "ms");
            System.out.println("Error Rate: " + summary.errorRate + "%");
            System.out.println("Memory Usage Peak: " + summary.maxMemoryUsed + "MB");

            // Assertions
            assertThat(completed).isTrue();
            assertThat(summary.errorRate).isLessThan(1.0); // Less than 1% error rate
            assertThat(summary.averageResponseTime).isLessThan(200.0); // Average response time under 200ms
            assertThat(summary.percentile95).isLessThan(500.0); // 95th percentile under 500ms
        } finally {
            // Cleanup
            testRunning.set(false);
            monitoringTask.cancel(false);
            cleanupTask.cancel(false);
            userTasks.forEach(task -> task.cancel(true));
            userSimulators.shutdown();
            monitoringExecutor.shutdown();
        }
    }

    private ScheduledFuture<?> startMonitoring() {
        return monitoringExecutor.scheduleAtFixedRate(() -> {
            EnduranceMetrics.Summary currentMetrics = metrics.generateSummary();
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);

            System.out.printf(
                "\rActive: %d, Success: %d, Failed: %d, Avg RT: %.2fms, Memory: %dMB    ",
                metrics.getActiveUsers(),
                currentMetrics.successfulRequests,
                currentMetrics.failedRequests,
                currentMetrics.averageResponseTime,
                usedMemory
            );
        }, 1, 1, TimeUnit.SECONDS);
    }

    private ScheduledFuture<?> scheduleCleanupTask() {
        return monitoringExecutor.scheduleAtFixedRate(() -> {
            try {
                int cleaned = tokenBlacklistService.cleanupExpiredTokens();
                if (cleaned > 0) {
                    System.out.printf("\nCleaned up %d expired tokens%n", cleaned);
                }
            } catch (Exception e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private class UserSimulator implements Runnable {
        private final Instant endTime;
        private final Random random = new Random();

        public UserSimulator(Instant endTime) {
            this.endTime = endTime;
        }

        @Override
        public void run() {
            while (testRunning.get() && Instant.now().isBefore(endTime)) {
                try {
                    metrics.incrementActiveUsers();
                    performUserActions();
                    Thread.sleep(random.nextInt(1000) + 500); // Random delay between actions
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    metrics.recordFailure();
                } finally {
                    metrics.decrementActiveUsers();
                }
            }
        }

        private void performUserActions() throws Exception {
            // Login
            Instant start = Instant.now();
            try {
                performLoginRequest();
                metrics.recordSuccess(Duration.between(start, Instant.now()).toMillis());
            } catch (Exception e) {
                metrics.recordFailure();
                throw e;
            }

            // Token operations
            String token = jwtService.generateToken(testUser);
            if (random.nextBoolean()) {
                tokenBlacklistService.blacklist(token, false, 
                    System.currentTimeMillis() + 3600000,
                    "testuser", "endurance_test");
            }
        }
    }

    private void performLoginRequest() throws Exception {
        ResetPasswordRequest.LoginRequest loginRequest = ResetPasswordRequest.LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
                .andExpect(status().isOk());
    }

    private String toJson(Object obj) throws Exception {
        return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
    }

    private static class EnduranceMetrics {
        private final AtomicInteger activeUsers = new AtomicInteger(0);
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong successfulRequests = new AtomicLong(0);
        private final AtomicLong failedRequests = new AtomicLong(0);
        private final ConcurrentSkipListMap<Long, AtomicLong> responseTimes = new ConcurrentSkipListMap<>();
        private final AtomicLong maxMemoryUsed = new AtomicLong(0);

        public void incrementActiveUsers() {
            activeUsers.incrementAndGet();
        }

        public void decrementActiveUsers() {
            activeUsers.decrementAndGet();
        }

        public int getActiveUsers() {
            return activeUsers.get();
        }

        public void recordSuccess(long responseTime) {
            successfulRequests.incrementAndGet();
            totalRequests.incrementAndGet();
            responseTimes.computeIfAbsent(responseTime, k -> new AtomicLong()).incrementAndGet();
            updateMemoryUsage();
        }

        public void recordFailure() {
            failedRequests.incrementAndGet();
            totalRequests.incrementAndGet();
            updateMemoryUsage();
        }

        private void updateMemoryUsage() {
            Runtime runtime = Runtime.getRuntime();
            long currentMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            maxMemoryUsed.updateAndGet(current -> Math.max(current, currentMemory));
        }

        public Summary generateSummary() {
            long total = totalRequests.get();
            if (total == 0) {
                return new Summary(0, 0, 0, 0.0, 0.0, 0.0, 0);
            }

            double errorRate = (failedRequests.get() * 100.0) / total;

            // Calculate response time metrics
            List<Long> times = new ArrayList<>();
            responseTimes.forEach((time, count) -> {
                for (long i = 0; i < count.get(); i++) {
                    times.add(time);
                }
            });

            double avgResponseTime = times.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);

            // Calculate 95th percentile
            Collections.sort(times);
            int index95 = (int) Math.ceil(times.size() * 0.95) - 1;
            double percentile95 = index95 >= 0 ? times.get(index95) : 0.0;

            return new Summary(
                total,
                successfulRequests.get(),
                failedRequests.get(),
                avgResponseTime,
                percentile95,
                errorRate,
                maxMemoryUsed.get()
            );
        }

        public static class Summary {
            public final long totalRequests;
            public final long successfulRequests;
            public final long failedRequests;
            public final double averageResponseTime;
            public final double percentile95;
            public final double errorRate;
            public final long maxMemoryUsed;

            public Summary(long totalRequests, long successfulRequests, long failedRequests,
                         double averageResponseTime, double percentile95, double errorRate,
                         long maxMemoryUsed) {
                this.totalRequests = totalRequests;
                this.successfulRequests = successfulRequests;
                this.failedRequests = failedRequests;
                this.averageResponseTime = averageResponseTime;
                this.percentile95 = percentile95;
                this.errorRate = errorRate;
                this.maxMemoryUsed = maxMemoryUsed;
            }
        }
    }
}
