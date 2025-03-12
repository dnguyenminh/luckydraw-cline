package vn.com.fecredit.app.performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityThreadTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    private User testUser;
    private ExecutorService executorService;
    private ThreadAnalyzer threadAnalyzer;
    private AtomicBoolean testRunning;

    @BeforeEach
    void setUp() {
        testUser = (User) userService.loadUserByUsername("testuser");
        executorService = Executors.newFixedThreadPool(20);
        threadAnalyzer = new ThreadAnalyzer();
        testRunning = new AtomicBoolean(true);
    }

    @Test
    void concurrentTokenOperations_ThreadBehavior() throws Exception {
        // Given
        int concurrentOperations = 100;
        int operationsPerThread = 50;
        ThreadSnapshot baselineSnapshot = threadAnalyzer.takeSnapshot();
        CountDownLatch completionLatch = new CountDownLatch(concurrentOperations);
        List<Future<?>> futures = new ArrayList<>();

        // When
        Instant start = Instant.now();
        for (int i = 0; i < concurrentOperations; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    performMixedTokenOperations(operationsPerThread);
                } finally {
                    completionLatch.countDown();
                }
            }));
        }

        // Monitor thread states during execution
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
        List<ThreadSnapshot> snapshots = Collections.synchronizedList(new ArrayList<>());
        ScheduledFuture<?> monitoringTask = monitor.scheduleAtFixedRate(() -> {
            snapshots.add(threadAnalyzer.takeSnapshot());
        }, 0, 100, TimeUnit.MILLISECONDS);

        // Wait for completion
        completionLatch.await(5, TimeUnit.MINUTES);
        Duration duration = Duration.between(start, Instant.now());
        monitoringTask.cancel(false);

        // Then
        ThreadSnapshot finalSnapshot = threadAnalyzer.takeSnapshot();
        ThreadAnalysis analysis = threadAnalyzer.analyzeSnapshots(baselineSnapshot, snapshots, finalSnapshot);
        printThreadAnalysis(analysis, concurrentOperations * operationsPerThread, duration);

        // Assertions
        assertThreadBehavior(analysis);
        assertNoDeadlocks(analysis);
        assertThreadPoolEfficiency(analysis);
    }

    @Test
    void threadContention_UnderLoad() throws Exception {
        // Given
        int concurrentThreads = 20;
        int operationsPerThread = 1000;
        ThreadSnapshot baselineSnapshot = threadAnalyzer.takeSnapshot();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(concurrentThreads);
        List<Future<ThreadMetrics>> futures = new ArrayList<>();

        // When
        for (int i = 0; i < concurrentThreads; i++) {
            futures.add(executorService.submit(() -> {
                startLatch.await(); // Synchronize start
                Instant threadStart = Instant.now();
                long blockedTime = 0;
                long waitTime = 0;

                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        ThreadInfo threadInfo = threadAnalyzer.getCurrentThreadInfo();
                        blockedTime += threadInfo.getBlockedTime();
                        waitTime += threadInfo.getWaitedTime();
                        performTokenOperation();
                    }
                } finally {
                    completionLatch.countDown();
                }

                return new ThreadMetrics(
                    Thread.currentThread().getName(),
                    Duration.between(threadStart, Instant.now()),
                    blockedTime,
                    waitTime
                );
            }));
        }

        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for completion
        completionLatch.await(5, TimeUnit.MINUTES);
        
        // Collect results
        List<ThreadMetrics> metrics = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        // Then
        ThreadSnapshot finalSnapshot = threadAnalyzer.takeSnapshot();
        printContentionAnalysis(metrics);
        assertThreadContention(metrics);
    }

    private void performMixedTokenOperations(int count) {
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            performTokenOperation();
            if (random.nextBoolean()) {
                Thread.yield(); // Simulate real-world context switching
            }
        }
    }

    private void performTokenOperation() {
        try {
            String token = jwtService.generateToken(testUser);
            jwtService.isTokenValid(token, testUser);
            if (new Random().nextBoolean()) {
                tokenBlacklistService.blacklist(
                    token,
                    false,
                    System.currentTimeMillis() + 3600000,
                    "testuser",
                    "thread_test"
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void assertThreadBehavior(ThreadAnalysis analysis) {
        assertThat(analysis.maxThreadCount)
            .as("Thread count should not exceed reasonable limit")
            .isLessThan(100);

        assertThat(analysis.averageActiveThreads)
            .as("Average active threads should be reasonable")
            .isLessThan(50.0);

        assertThat(analysis.blockedThreadPercentage)
            .as("Blocked thread percentage should be minimal")
            .isLessThan(10.0);
    }

    private void assertNoDeadlocks(ThreadAnalysis analysis) {
        assertThat(analysis.deadlockCount)
            .as("No deadlocks should be detected")
            .isZero();
    }

    private void assertThreadPoolEfficiency(ThreadAnalysis analysis) {
        assertThat(analysis.threadPoolUtilization)
            .as("Thread pool utilization should be efficient")
            .isBetween(60.0, 100.0);
    }

    private void assertThreadContention(List<ThreadMetrics> metrics) {
        double avgBlockedTime = metrics.stream()
                .mapToLong(m -> m.blockedTime)
                .average()
                .orElse(0.0);

        double avgWaitTime = metrics.stream()
                .mapToLong(m -> m.waitTime)
                .average()
                .orElse(0.0);

        assertThat(avgBlockedTime)
            .as("Average blocked time should be minimal")
            .isLessThan(1000); // Less than 1 second

        assertThat(avgWaitTime)
            .as("Average wait time should be reasonable")
            .isLessThan(2000); // Less than 2 seconds
    }

    private void printThreadAnalysis(ThreadAnalysis analysis, int totalOperations, Duration duration) {
        System.out.println("\nThread Analysis Report");
        System.out.println("=====================");
        System.out.printf("Total Operations: %d%n", totalOperations);
        System.out.printf("Duration: %.2f seconds%n", duration.toMillis() / 1000.0);
        System.out.printf("Maximum Thread Count: %d%n", analysis.maxThreadCount);
        System.out.printf("Average Active Threads: %.2f%n", analysis.averageActiveThreads);
        System.out.printf("Thread Pool Utilization: %.2f%%%n", analysis.threadPoolUtilization);
        System.out.printf("Blocked Thread Percentage: %.2f%%%n", analysis.blockedThreadPercentage);
        System.out.printf("Deadlocks Detected: %d%n", analysis.deadlockCount);
        System.out.printf("Operations/Second: %.2f%n", 
            totalOperations / (duration.toMillis() / 1000.0));
    }

    private void printContentionAnalysis(List<ThreadMetrics> metrics) {
        System.out.println("\nThread Contention Analysis");
        System.out.println("=========================");
        System.out.printf("Total Threads: %d%n", metrics.size());
        
        DoubleSummaryStatistics blockedStats = metrics.stream()
                .mapToDouble(m -> m.blockedTime)
                .summaryStatistics();
        
        DoubleSummaryStatistics waitStats = metrics.stream()
                .mapToDouble(m -> m.waitTime)
                .summaryStatistics();

        System.out.println("\nBlocked Time Statistics (ms):");
        System.out.printf("Avg: %.2f, Min: %.2f, Max: %.2f%n",
            blockedStats.getAverage(),
            blockedStats.getMin(),
            blockedStats.getMax());

        System.out.println("\nWait Time Statistics (ms):");
        System.out.printf("Avg: %.2f, Min: %.2f, Max: %.2f%n",
            waitStats.getAverage(),
            waitStats.getMin(),
            waitStats.getMax());
    }

    private static class ThreadAnalyzer {
        private final ThreadMXBean threadMXBean;

        public ThreadAnalyzer() {
            this.threadMXBean = ManagementFactory.getThreadMXBean();
            this.threadMXBean.setThreadContentionMonitoringEnabled(true);
        }

        public ThreadSnapshot takeSnapshot() {
            long[] threadIds = threadMXBean.getAllThreadIds();
            ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds, true, true);
            
            return new ThreadSnapshot(
                Arrays.stream(threadInfos)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()),
                Instant.now()
            );
        }

        public ThreadInfo getCurrentThreadInfo() {
            return threadMXBean.getThreadInfo(Thread.currentThread().getId());
        }

        public ThreadAnalysis analyzeSnapshots(
                ThreadSnapshot baseline,
                List<ThreadSnapshot> snapshots,
                ThreadSnapshot finalSnapshot) {
            
            int maxThreadCount = snapshots.stream()
                    .mapToInt(s -> s.threadInfos.size())
                    .max()
                    .orElse(0);

            double averageActiveThreads = snapshots.stream()
                    .mapToInt(s -> (int) s.threadInfos.stream()
                        .filter(t -> t.getThreadState() == Thread.State.RUNNABLE)
                        .count())
                    .average()
                    .orElse(0.0);

            double blockedPercentage = snapshots.stream()
                    .mapToDouble(s -> (double) s.threadInfos.stream()
                        .filter(t -> t.getThreadState() == Thread.State.BLOCKED)
                        .count() / s.threadInfos.size() * 100)
                    .average()
                    .orElse(0.0);

            int poolSize = 20; // From setUp()
            double utilization = snapshots.stream()
                    .mapToDouble(s -> (double) s.threadInfos.stream()
                        .filter(t -> t.getThreadName().contains("pool"))
                        .filter(t -> t.getThreadState() == Thread.State.RUNNABLE)
                        .count() / poolSize * 100)
                    .average()
                    .orElse(0.0);

            return new ThreadAnalysis(
                maxThreadCount,
                averageActiveThreads,
                utilization,
                blockedPercentage,
                threadMXBean.findDeadlockedThreads() != null ? 
                    threadMXBean.findDeadlockedThreads().length : 0
            );
        }
    }

    private record ThreadSnapshot(List<ThreadInfo> threadInfos, Instant timestamp) {}

    private record ThreadAnalysis(
        int maxThreadCount,
        double averageActiveThreads,
        double threadPoolUtilization,
        double blockedThreadPercentage,
        int deadlockCount
    ) {}

    private record ThreadMetrics(
        String threadName,
        Duration executionTime,
        long blockedTime,
        long waitTime
    ) {}
}
