package vn.com.fecredit.app.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import vn.com.fecredit.app.entity.User;
import vn.com.fecredit.app.security.JwtService;
import vn.com.fecredit.app.service.TokenBlacklistService;
import vn.com.fecredit.app.service.UserService;

import java.lang.management.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityResourceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    private User testUser;
    private ExecutorService executorService;
    private ResourceMonitor resourceMonitor;
    private AtomicBoolean testRunning;

    @BeforeEach
    void setUp() {
        testUser = (User) userService.loadUserByUsername("testuser");
        executorService = Executors.newFixedThreadPool(10);
        resourceMonitor = new ResourceMonitor();
        testRunning = new AtomicBoolean(true);
    }

    @Test
    void tokenGeneration_ResourceUsage() throws Exception {
        // Given
        int totalTokens = 10000;
        ResourceMetrics baseline = resourceMonitor.captureMetrics();
        List<String> tokens = new ArrayList<>();
        AtomicLong totalTime = new AtomicLong(0);

        // When
        Instant start = Instant.now();
        for (int i = 0; i < totalTokens; i++) {
            Instant tokenStart = Instant.now();
            tokens.add(jwtService.generateToken(testUser));
            totalTime.addAndGet(Duration.between(tokenStart, Instant.now()).toNanos());
        }
        Duration duration = Duration.between(start, Instant.now());

        // Then
        ResourceMetrics endMetrics = resourceMonitor.captureMetrics();
        ResourceUsageReport report = resourceMonitor.generateReport(baseline, endMetrics, duration);
        
        printResourceReport("Token Generation", report, totalTokens, totalTime.get());
        assertResourceUsage(report, totalTokens);
    }

    @Test
    void tokenBlacklisting_ResourceUsage() throws Exception {
        // Given
        int totalOperations = 5000;
        int batchSize = 100;
        ResourceMetrics baseline = resourceMonitor.captureMetrics();
        AtomicLong totalTime = new AtomicLong(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // When
        Instant start = Instant.now();
        for (int i = 0; i < totalOperations; i += batchSize) {
            List<String> batch = generateTokenBatch(Math.min(batchSize, totalOperations - i));
            futures.add(CompletableFuture.runAsync(() -> {
                Instant batchStart = Instant.now();
                batch.forEach(token -> 
                    tokenBlacklistService.blacklist(token, false, 
                        System.currentTimeMillis() + 3600000,
                        "testuser", "resource_test"));
                totalTime.addAndGet(Duration.between(batchStart, Instant.now()).toNanos());
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        Duration duration = Duration.between(start, Instant.now());

        // Then
        ResourceMetrics endMetrics = resourceMonitor.captureMetrics();
        ResourceUsageReport report = resourceMonitor.generateReport(baseline, endMetrics, duration);
        
        printResourceReport("Token Blacklisting", report, totalOperations, totalTime.get());
        assertResourceUsage(report, totalOperations);
    }

    @Test
    void tokenValidation_ResourceUsage() throws Exception {
        // Given
        int totalValidations = 20000;
        String token = jwtService.generateToken(testUser);
        ResourceMetrics baseline = resourceMonitor.captureMetrics();
        AtomicLong totalTime = new AtomicLong(0);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        // When
        Instant start = Instant.now();
        for (int i = 0; i < totalValidations; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                Instant validationStart = Instant.now();
                boolean result = jwtService.isTokenValid(token, testUser);
                totalTime.addAndGet(Duration.between(validationStart, Instant.now()).toNanos());
                return result;
            }, executorService));
        }

        List<Boolean> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        Duration duration = Duration.between(start, Instant.now());

        // Then
        ResourceMetrics endMetrics = resourceMonitor.captureMetrics();
        ResourceUsageReport report = resourceMonitor.generateReport(baseline, endMetrics, duration);
        
        printResourceReport("Token Validation", report, totalValidations, totalTime.get());
        assertResourceUsage(report, totalValidations);
        assertThat(results).allMatch(valid -> valid);
    }

    private List<String> generateTokenBatch(int size) {
        List<String> tokens = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            tokens.add(jwtService.generateToken(testUser));
        }
        return tokens;
    }

    private void assertResourceUsage(ResourceUsageReport report, int operations) {
        assertThat(report.cpuTimePerOperation)
            .as("CPU time per operation should be reasonable")
            .isLessThan(Duration.ofMillis(10).toNanos());

        assertThat(report.allocatedMemoryPerOperation)
            .as("Memory allocation per operation should be reasonable")
            .isLessThan(1024 * 10); // 10KB per operation

        assertThat(report.gcPauseTimeRatio)
            .as("GC pause time ratio should be minimal")
            .isLessThan(0.05); // Less than 5% time in GC
    }

    private void printResourceReport(String operation, ResourceUsageReport report, 
            int totalOperations, long totalProcessingTime) {
        System.out.println("\n" + operation + " Resource Usage Report");
        System.out.println("=====================================");
        System.out.printf("Total Operations: %d%n", totalOperations);
        System.out.printf("Total Duration: %.2f seconds%n", report.duration.toMillis() / 1000.0);
        System.out.printf("Operations/Second: %.2f%n", 
            totalOperations / (report.duration.toMillis() / 1000.0));
        System.out.printf("Average Processing Time: %.3f ms%n", 
            totalProcessingTime / (double) totalOperations / 1_000_000);
        System.out.printf("CPU Time per Operation: %.3f ms%n", 
            report.cpuTimePerOperation / 1_000_000.0);
        System.out.printf("Memory Allocated per Operation: %.2f KB%n", 
            report.allocatedMemoryPerOperation / 1024.0);
        System.out.printf("GC Pause Time Ratio: %.2f%%%n", report.gcPauseTimeRatio * 100);
        System.out.printf("Total Memory Growth: %.2f MB%n", report.totalMemoryGrowth / (1024.0 * 1024));
    }

    private static class ResourceMonitor {
        private final OperatingSystemMXBean osMXBean;
        private final MemoryMXBean memoryMXBean;
        private final List<GarbageCollectorMXBean> gcBeans;
        // private final ThreadMXBean threadMXBean;

        public ResourceMonitor() {
            this.osMXBean = ManagementFactory.getOperatingSystemMXBean();
            this.memoryMXBean = ManagementFactory.getMemoryMXBean();
            this.gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
            this.threadMXBean = ManagementFactory.getThreadMXBean();
        }

        public ResourceMetrics captureMetrics() {
            return new ResourceMetrics(
                getCpuTime(),
                getGcTime(),
                getAllocatedMemory(),
                getTotalMemory(),
                Instant.now()
            );
        }

        private long getCpuTime() {
            if (osMXBean instanceof com.sun.management.OperatingSystemMXBean sunOsMXBean) {
                return sunOsMXBean.getProcessCpuTime();
            }
            return 0;
        }

        private long getGcTime() {
            return gcBeans.stream()
                    .mapToLong(GarbageCollectorMXBean::getCollectionTime)
                    .sum();
        }

        private long getAllocatedMemory() {
            return memoryMXBean.getHeapMemoryUsage().getUsed() +
                   memoryMXBean.getNonHeapMemoryUsage().getUsed();
        }

        private long getTotalMemory() {
            return memoryMXBean.getHeapMemoryUsage().getCommitted() +
                   memoryMXBean.getNonHeapMemoryUsage().getCommitted();
        }

        public ResourceUsageReport generateReport(ResourceMetrics start, 
                ResourceMetrics end, Duration testDuration) {
            long cpuTimeDiff = end.cpuTime - start.cpuTime;
            long gcTimeDiff = end.gcTime - start.gcTime;
            long allocatedMemoryDiff = end.allocatedMemory - start.allocatedMemory;
            long totalMemoryDiff = end.totalMemory - start.totalMemory;
            Duration actualDuration = Duration.between(start.timestamp, end.timestamp);

            return new ResourceUsageReport(
                actualDuration,
                cpuTimeDiff,
                (double) gcTimeDiff / actualDuration.toMillis(),
                allocatedMemoryDiff,
                totalMemoryDiff
            );
        }
    }

    private record ResourceMetrics(
        long cpuTime,
        long gcTime,
        long allocatedMemory,
        long totalMemory,
        Instant timestamp
    ) {}

    private record ResourceUsageReport(
        Duration duration,
        long cpuTimePerOperation,
        double gcPauseTimeRatio,
        long allocatedMemoryPerOperation,
        long totalMemoryGrowth
    ) {}
}
