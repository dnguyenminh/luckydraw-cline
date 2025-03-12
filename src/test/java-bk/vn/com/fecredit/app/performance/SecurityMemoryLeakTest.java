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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityMemoryLeakTest {

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
    private MemoryMXBean memoryMXBean;
    private List<MemorySnapshot> memorySnapshots;
    private AtomicBoolean testRunning;

    @BeforeEach
    void setUp() {
        testUser = (User) userService.loadUserByUsername("testuser");
        executorService = Executors.newFixedThreadPool(20);
        monitoringExecutor = Executors.newSingleThreadScheduledExecutor();
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        memorySnapshots = new CopyOnWriteArrayList<>();
        testRunning = new AtomicBoolean(true);

        // Force GC before test
        System.gc();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void tokenOperations_ShouldNotLeakMemory() throws Exception {
        // Given
        int testDurationMinutes = 10;
        int operationsPerSecond = 50;
        AtomicInteger totalOperations = new AtomicInteger(0);
        CountDownLatch testCompletion = new CountDownLatch(1);

        // Start memory monitoring
        ScheduledFuture<?> monitoringTask = startMemoryMonitoring();

        try {
            // When - Run intensive token operations
            Instant startTime = Instant.now();
            Instant endTime = startTime.plus(Duration.ofMinutes(testDurationMinutes));

            // Submit token operations
            while (Instant.now().isBefore(endTime) && testRunning.get()) {
                for (int i = 0; i < operationsPerSecond; i++) {
                    executorService.submit(() -> {
                        try {
                            performTokenOperations();
                            totalOperations.incrementAndGet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                Thread.sleep(1000); // Wait 1 second before next batch
            }

            testCompletion.await(testDurationMinutes + 1, TimeUnit.MINUTES);

            // Then - Analyze memory usage
            List<MemoryTrend> trends = analyzeMemoryTrends();
            printMemoryAnalysis(trends, totalOperations.get());

            // Assertions
            assertThat(detectMemoryLeak(trends))
                .as("No significant memory leak detected")
                .isFalse();

            assertThat(getMaxHeapIncrease(trends))
                .as("Maximum heap increase should be less than 50%")
                .isLessThan(50.0);

        } finally {
            testRunning.set(false);
            monitoringTask.cancel(false);
            executorService.shutdown();
            monitoringExecutor.shutdown();
        }
    }

    private void performTokenOperations() throws Exception {
        // Generate and blacklist tokens
        String accessToken = jwtService.generateToken(testUser);
        String refreshToken = jwtService.generateToken(
            Map.of("refresh", true),
            testUser
        );

        // Blacklist tokens
        tokenBlacklistService.blacklist(
            accessToken,
            false,
            System.currentTimeMillis() + 3600000,
            "testuser",
            "memory_test"
        );

        tokenBlacklistService.blacklist(
            refreshToken,
            true,
            System.currentTimeMillis() + 86400000,
            "testuser",
            "memory_test"
        );

        // Verify tokens are blacklisted
        assertThat(tokenBlacklistService.isBlacklisted(accessToken)).isTrue();
        assertThat(tokenBlacklistService.isBlacklisted(refreshToken)).isTrue();

        // Simulate authentication
        performLoginRequest();
    }

    private ScheduledFuture<?> startMemoryMonitoring() {
        return monitoringExecutor.scheduleAtFixedRate(() -> {
            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

            memorySnapshots.add(new MemorySnapshot(
                Instant.now(),
                heapUsage.getUsed(),
                heapUsage.getCommitted(),
                nonHeapUsage.getUsed(),
                nonHeapUsage.getCommitted()
            ));

            // Print current memory usage
            System.out.printf("\rHeap: %d MB, NonHeap: %d MB    ",
                heapUsage.getUsed() / (1024 * 1024),
                nonHeapUsage.getUsed() / (1024 * 1024));

        }, 0, 1, TimeUnit.SECONDS);
    }

    private List<MemoryTrend> analyzeMemoryTrends() {
        List<MemoryTrend> trends = new ArrayList<>();
        int windowSize = 60; // 1-minute windows

        for (int i = 0; i < memorySnapshots.size() - windowSize; i += windowSize) {
            List<MemorySnapshot> window = memorySnapshots.subList(i, i + windowSize);
            double heapGrowthRate = calculateGrowthRate(window, MemorySnapshot::heapUsed);
            double nonHeapGrowthRate = calculateGrowthRate(window, MemorySnapshot::nonHeapUsed);

            trends.add(new MemoryTrend(
                Duration.between(memorySnapshots.get(0).timestamp, window.get(0).timestamp),
                heapGrowthRate,
                nonHeapGrowthRate
            ));
        }

        return trends;
    }

    private double calculateGrowthRate(List<MemorySnapshot> snapshots, 
            java.util.function.ToLongFunction<MemorySnapshot> memoryExtractor) {
        if (snapshots.isEmpty()) return 0.0;

        long initial = memoryExtractor.applyAsLong(snapshots.get(0));
        long last = memoryExtractor.applyAsLong(snapshots.get(snapshots.size() - 1));
        
        if (initial == 0) return 0.0;
        return ((double) (last - initial) / initial) * 100.0;
    }

    private boolean detectMemoryLeak(List<MemoryTrend> trends) {
        if (trends.isEmpty()) return false;

        // Calculate average growth rate
        double avgHeapGrowth = trends.stream()
                .mapToDouble(MemoryTrend::heapGrowthRate)
                .average()
                .orElse(0.0);

        // If average growth rate is consistently positive and significant
        return avgHeapGrowth > 10.0; // More than 10% growth rate indicates potential leak
    }

    private double getMaxHeapIncrease(List<MemoryTrend> trends) {
        return trends.stream()
                .mapToDouble(MemoryTrend::heapGrowthRate)
                .max()
                .orElse(0.0);
    }

    private void printMemoryAnalysis(List<MemoryTrend> trends, int totalOperations) {
        System.out.println("\n\nMemory Analysis Results:");
        System.out.println("=======================");
        System.out.println("Total Operations: " + totalOperations);
        System.out.println("Monitoring Windows: " + trends.size());
        
        if (!trends.isEmpty()) {
            OptionalDouble avgHeapGrowth = trends.stream()
                    .mapToDouble(MemoryTrend::heapGrowthRate)
                    .average();
            OptionalDouble avgNonHeapGrowth = trends.stream()
                    .mapToDouble(MemoryTrend::nonHeapGrowthRate)
                    .average();

            System.out.printf("Average Heap Growth Rate: %.2f%%%n", avgHeapGrowth.orElse(0.0));
            System.out.printf("Average Non-Heap Growth Rate: %.2f%%%n", avgNonHeapGrowth.orElse(0.0));
            System.out.printf("Maximum Heap Growth Rate: %.2f%%%n", getMaxHeapIncrease(trends));
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

    private record MemorySnapshot(
            Instant timestamp,
            long heapUsed,
            long heapCommitted,
            long nonHeapUsed,
            long nonHeapCommitted) {}

    private record MemoryTrend(
            Duration timeFromStart,
            double heapGrowthRate,
            double nonHeapGrowthRate) {}
}
