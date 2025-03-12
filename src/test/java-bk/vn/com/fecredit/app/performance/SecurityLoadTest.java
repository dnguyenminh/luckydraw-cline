package vn.com.fecredit.app.performance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import vn.com.fecredit.app.dto.ResetPasswordRequest;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.security.JwtService;
import vn.com.fecredit.app.service.TokenBlacklistService;
import vn.com.fecredit.app.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityLoadTest {

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
    private ExecutorService metricsExecutor;
    private ScheduledExecutorService monitoringExecutor;

    @BeforeEach
    void setUp() {
        testUser = (User) userService.loadUserByUsername("testuser");
        executorService = Executors.newFixedThreadPool(20);
        metricsExecutor = Executors.newSingleThreadExecutor();
        monitoringExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    @Test
    void loginEndpoint_UnderLoad() throws Exception {
        // Given
        int totalRequests = 1000;
        int concurrentUsers = 50;
        Semaphore semaphore = new Semaphore(concurrentUsers);
        List<Future<Long>> responseTimes = new ArrayList<>();
        ConcurrentHashMap<Integer, Long> metrics = new ConcurrentHashMap<>();

        // Start metrics collection
        ScheduledFuture<?> metricsFuture = startMetricsCollection(metrics);

        try {
            // When
            Instant startTime = Instant.now();

            IntStream.range(0, totalRequests).forEach(i -> {
                try {
                    semaphore.acquire();
                    responseTimes.add(executorService.submit(() -> {
                        try {
                            Instant requestStart = Instant.now();
                            performLoginRequest();
                            long responseTime = Duration.between(requestStart, Instant.now()).toMillis();
                            metrics.put(i, responseTime);
                            return responseTime;
                        } finally {
                            semaphore.release();
                        }
                    }));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            });

            // Wait for all requests to complete
            List<Long> completedTimes = new ArrayList<>();
            for (Future<Long> future : responseTimes) {
                completedTimes.add(future.get(30, TimeUnit.SECONDS));
            }

            // Then
            long totalDuration = Duration.between(startTime, Instant.now()).toMillis();
            double averageResponseTime = completedTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            long maxResponseTime = completedTimes.stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0);

            System.out.println("Load Test Results:");
            System.out.println("Total Duration: " + totalDuration + "ms");
            System.out.println("Average Response Time: " + averageResponseTime + "ms");
            System.out.println("Max Response Time: " + maxResponseTime + "ms");
            System.out.println("Requests per Second: " + (totalRequests * 1000.0 / totalDuration));

            assertThat(averageResponseTime).isLessThan(200); // Average response time under 200ms
            assertThat(maxResponseTime).isLessThan(1000); // Max response time under 1 second
        } finally {
            metricsFuture.cancel(true);
        }
    }

    @Test
    void tokenBlacklist_UnderLoad() throws Exception {
        // Given
        int totalTokens = 1000;
        int batchSize = 50;
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < totalTokens; i++) {
            tokens.add(jwtService.generateToken(testUser));
        }

        List<Future<Long>> blacklistTimes = new ArrayList<>();
        ConcurrentHashMap<Integer, Long> metrics = new ConcurrentHashMap<>();

        // Start metrics collection
        ScheduledFuture<?> metricsFuture = startMetricsCollection(metrics);

        try {
            // When
            Instant startTime = Instant.now();

            for (int i = 0; i < tokens.size(); i += batchSize) {
                int batchEnd = Math.min(i + batchSize, tokens.size());
                List<String> batch = new ArrayList<>(tokens.subList(i, batchEnd));
                
                blacklistTimes.add(executorService.submit(() -> {
                    Instant batchStart = Instant.now();
                    batch.parallelStream().forEach(token -> 
                        tokenBlacklistService.blacklist(token, false, 
                                System.currentTimeMillis() + 3600000,
                                "testuser", "load_test"));
                    return Duration.between(batchStart, Instant.now()).toMillis();
                }));
            }

            // Wait for all batches to complete
            List<Long> completedTimes = new ArrayList<>();
            for (Future<Long> future : blacklistTimes) {
                completedTimes.add(future.get(30, TimeUnit.SECONDS));
            }

            // Then
            long totalDuration = Duration.between(startTime, Instant.now()).toMillis();
            double averageBatchTime = completedTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            long maxBatchTime = completedTimes.stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0);

            System.out.println("Token Blacklist Load Test Results:");
            System.out.println("Total Duration: " + totalDuration + "ms");
            System.out.println("Average Batch Time: " + averageBatchTime + "ms");
            System.out.println("Max Batch Time: " + maxBatchTime + "ms");
            System.out.println("Tokens per Second: " + (totalTokens * 1000.0 / totalDuration));

            assertThat(averageBatchTime).isLessThan(500); // Average batch time under 500ms
            assertThat(maxBatchTime).isLessThan(2000); // Max batch time under 2 seconds
        } finally {
            metricsFuture.cancel(true);
        }
    }

    private ScheduledFuture<?> startMetricsCollection(ConcurrentHashMap<Integer, Long> metrics) {
        return monitoringExecutor.scheduleAtFixedRate(() -> {
            double currentAverage = metrics.values().stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            System.out.printf("Current metrics - Count: %d, Avg Response Time: %.2fms%n", 
                    metrics.size(), currentAverage);
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void performLoginRequest() throws Exception {
        ResetPasswordRequest.LoginRequest loginRequest = ResetPasswordRequest.LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String toJson(Object obj) throws Exception {
        return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
    }
}
