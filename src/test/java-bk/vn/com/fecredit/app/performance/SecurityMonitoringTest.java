package vn.com.fecredit.app.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityMonitoringTest {

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
    private AtomicBoolean testRunning;
    private SystemMetricsCollector metricsCollector;

    @BeforeEach
    void setUp() {
        testUser = (User) userService.loadUserByUsername("testuser");
        executorService = Executors.newFixedThreadPool(20);
        monitoringExecutor = Executors.newScheduledThreadPool(2);
        testRunning = new AtomicBoolean(true);
        metricsCollector = new SystemMetricsCollector();
    }

    @Test
    void securityOperations_SystemMetrics() throws Exception {
        // Given
        int testDurationMinutes = 5;
        int concurrentUsers = 10;
        AtomicInteger completedOperations = new AtomicInteger(0);
        CountDownLatch testCompletion = new CountDownLatch(1);

        // Start monitoring
        ScheduledFuture<?> monitoringTask = startSystemMonitoring();

        try {
            // When
            Instant startTime = Instant.now();
            Instant endTime = startTime.plus(Duration.ofMinutes(testDurationMinutes));

            // Launch concurrent users
            List<Future<?>> userTasks = new ArrayList<>();
            for (int i = 0; i < concurrentUsers; i++) {
                userTasks.add(executorService.submit(
                    new SecurityOperationsSimulator(endTime, completedOperations)));
            }

            // Wait for completion
            testCompletion.await(testDurationMinutes + 1, TimeUnit.MINUTES);

            // Then
            SystemMetricsCollector.SystemMetricsSummary summary = metricsCollector.generateSummary();
            printSystemMetricsAnalysis(summary, completedOperations.get());

            // Assertions
            assertThat(summary.cpuUsage().average())
                .as("Average CPU usage should be under 80%")
                .isLessThan(80.0);

            assertThat(summary.threadCount().max())
                .as("Maximum thread count should not exceed system limit")
                .isLessThan(Thread.activeCount() * 2);

            assertThat(summary.gcCount())
                .as("GC count should be reasonable")
                .isLessThan(50);

        } finally {
            testRunning.set(false);
            monitoringTask.cancel(false);
            executorService.shutdown();
            monitoringExecutor.shutdown();
        }
    }

    private ScheduledFuture<?> startSystemMonitoring() {
        return monitoringExecutor.scheduleAtFixedRate(() -> {
            metricsCollector.collectMetrics();
            printCurrentMetrics();
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void printCurrentMetrics() {
        SystemMetricsCollector.Metrics current = metricsCollector.getCurrentMetrics();
        System.out.printf("\rCPU: %.1f%%, Threads: %d, Memory: %dMB, GC: %d    ",
            current.cpuUsage(),
            current.threadCount(),
            current.usedMemory(),
            current.gcCount());
    }

    private void printSystemMetricsAnalysis(
            SystemMetricsCollector.SystemMetricsSummary summary,
            int totalOperations) {
        System.out.println("\n\nSystem Metrics Analysis:");
        System.out.println("=======================");
        System.out.println("Total Operations: " + totalOperations);
        System.out.println("Duration: " + summary.duration().toMinutes() + " minutes");
        System.out.printf("CPU Usage - Avg: %.1f%%, Max: %.1f%%, Min: %.1f%%%n",
            summary.cpuUsage().average(),
            summary.cpuUsage().max(),
            summary.cpuUsage().min());
        System.out.printf("Thread Count - Avg: %.1f, Max: %d, Min: %d%n",
            summary.threadCount().average(),
            summary.threadCount().max(),
            summary.threadCount().min());
        System.out.printf("Memory Usage - Avg: %dMB, Max: %dMB, Min: %dMB%n",
            (long) summary.memoryUsage().average(),
            summary.memoryUsage().max(),
            summary.memoryUsage().min());
        System.out.println("Total GC Count: " + summary.gcCount());
        System.out.println("Operations/Second: " + 
            String.format("%.2f", totalOperations / summary.duration().toSeconds()));
    }

    private class SecurityOperationsSimulator implements Runnable {
        private final Instant endTime;
        private final AtomicInteger operationsCounter;
        private final Random random = new Random();

        public SecurityOperationsSimulator(Instant endTime, AtomicInteger operationsCounter) {
            this.endTime = endTime;
            this.operationsCounter = operationsCounter;
        }

        @Override
        public void run() {
            while (testRunning.get() && Instant.now().isBefore(endTime)) {
                try {
                    performSecurityOperations();
                    operationsCounter.incrementAndGet();
                    Thread.sleep(random.nextInt(500) + 100); // Random delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in security operations: " + e.getMessage());
                }
            }
        }

        private void performSecurityOperations() throws Exception {
            // Generate tokens
            String accessToken = jwtService.generateToken(testUser);
            String refreshToken = jwtService.generateToken(
                Map.of("refresh", true),
                testUser
            );

            // Validate tokens
            jwtService.isTokenValid(accessToken, testUser);
            jwtService.isTokenValid(refreshToken, testUser);

            // Blacklist some tokens
            if (random.nextBoolean()) {
                tokenBlacklistService.blacklist(
                    accessToken,
                    false,
                    System.currentTimeMillis() + 3600000,
                    "testuser",
                    "monitoring_test"
                );
            }
        }
    }

    private static class SystemMetricsCollector {
        private final OperatingSystemMXBean osMXBean;
        private final ThreadMXBean threadMXBean;
        private final List<GarbageCollectorMXBean> gcMXBeans;
        private final MemoryMXBean memoryMXBean;
        private final List<Metrics> metricsHistory;
        private final Instant startTime;
        private long lastGcCount = 0;

        public SystemMetricsCollector() {
            this.osMXBean = ManagementFactory.getOperatingSystemMXBean();
            this.threadMXBean = ManagementFactory.getThreadMXBean();
            this.gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
            this.memoryMXBean = ManagementFactory.getMemoryMXBean();
            this.metricsHistory = new CopyOnWriteArrayList<>();
            this.startTime = Instant.now();
        }

        public void collectMetrics() {
            double cpuUsage = getCpuUsage();
            int threadCount = threadMXBean.getThreadCount();
            long usedMemory = getUsedMemory();
            long gcCount = getGcCount();

            metricsHistory.add(new Metrics(
                Instant.now(),
                cpuUsage,
                threadCount,
                usedMemory,
                gcCount
            ));
        }

        public Metrics getCurrentMetrics() {
            return metricsHistory.isEmpty() ? 
                new Metrics(Instant.now(), 0, 0, 0, 0) : 
                metricsHistory.get(metricsHistory.size() - 1);
        }

        private double getCpuUsage() {
            if (osMXBean instanceof com.sun.management.OperatingSystemMXBean sunOsMXBean) {
                return sunOsMXBean.getProcessCpuLoad() * 100;
            }
            return -1;
        }

        private long getUsedMemory() {
            return memoryMXBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        }

        private long getGcCount() {
            return gcMXBeans.stream()
                    .mapToLong(GarbageCollectorMXBean::getCollectionCount)
                    .sum();
        }

        public SystemMetricsSummary generateSummary() {
            if (metricsHistory.isEmpty()) {
                return new SystemMetricsSummary(
                    Duration.ZERO,
                    new MetricsRange(0, 0, 0),
                    new MetricsRange(0, 0, 0),
                    new MetricsRange(0, 0, 0),
                    0
                );
            }

            List<Double> cpuUsages = metricsHistory.stream()
                    .mapToDouble(Metrics::cpuUsage)
                    .filter(cpu -> cpu >= 0)
                    .boxed()
                    .toList();

            List<Integer> threadCounts = metricsHistory.stream()
                    .map(Metrics::threadCount)
                    .toList();

            List<Long> memoryUsages = metricsHistory.stream()
                    .map(Metrics::usedMemory)
                    .toList();

            return new SystemMetricsSummary(
                Duration.between(startTime, Instant.now()),
                calculateMetricsRange(cpuUsages),
                calculateMetricsRange(threadCounts),
                calculateMetricsRange(memoryUsages),
                getGcCount() - lastGcCount
            );
        }

        private <T extends Number> MetricsRange calculateMetricsRange(List<T> values) {
            if (values.isEmpty()) {
                return new MetricsRange(0, 0, 0);
            }

            double min = values.stream()
                    .mapToDouble(Number::doubleValue)
                    .min()
                    .orElse(0);
            double max = values.stream()
                    .mapToDouble(Number::doubleValue)
                    .max()
                    .orElse(0);
            double avg = values.stream()
                    .mapToDouble(Number::doubleValue)
                    .average()
                    .orElse(0);

            return new MetricsRange(min, max, avg);
        }

        public record Metrics(
                Instant timestamp,
                double cpuUsage,
                int threadCount,
                long usedMemory,
                long gcCount) {}

        public record SystemMetricsSummary(
                Duration duration,
                MetricsRange cpuUsage,
                MetricsRange threadCount,
                MetricsRange memoryUsage,
                long gcCount) {}

        public record MetricsRange(
                double min,
                double max,
                double average) {}
    }
}
