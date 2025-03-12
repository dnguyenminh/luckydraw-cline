package vn.com.fecredit.app.performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

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
public class TokenOperationsCpuTest {

    @Autowired private JwtService jwtService;
    @Autowired private UserService userService;
    @Autowired private TokenBlacklistService tokenBlacklistService;

    private record CpuSnapshot(
        long timestamp,
        long threadId,
        long cpuTime,
        long userTime,
        long systemTime,
        double processCpuLoad,
        double systemCpuLoad
    ) {}

    private record OperationTiming(
        long totalTime,
        long tokenGenTime,
        long validationTime,
        long blacklistTime,
        long cpuTime,
        long userTime,
        long systemTime
    ) {}

    private record CpuMetrics(
        double avgCpuUsage,
        double peakCpuUsage,
        double avgUserTime,
        double avgSystemTime,
        double operationCpuCost,
        long totalCpuTime,
        int threadCount,
        double cpuEfficiency
    ) {}

    @Test
    public void shouldMonitorCpuUsage() throws Exception {
        // Given
        User testUser = (User) userService.loadUserByUsername("testuser");
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(1000);
        ConcurrentLinkedQueue<CpuSnapshot> snapshots = new ConcurrentLinkedQueue<>();
        ConcurrentHashMap<Integer, OperationTiming> timings = new ConcurrentHashMap<>();
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();

        // CPU monitoring setup
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        threadBean.setThreadCpuTimeEnabled(true);

        long startTime = System.currentTimeMillis();
        Set<Long> monitoredThreads = ConcurrentHashMap.newKeySet();

        try {
            // Start CPU monitoring
            monitor.scheduleAtFixedRate(() -> {
                long now = System.nanoTime();
                double processCpu = getProcessCpuLoad(osBean);
                double systemCpu = getSystemCpuLoad(osBean);

                monitoredThreads.forEach(threadId -> {
                    long cpuTime = threadBean.getThreadCpuTime(threadId);
                    long userTime = threadBean.getThreadUserTime(threadId);
                    snapshots.add(new CpuSnapshot(
                        now,
                        threadId,
                        cpuTime,
                        userTime,
                        cpuTime - userTime,
                        processCpu,
                        systemCpu
                    ));
                });
            }, 0, 100, TimeUnit.MILLISECONDS);

            // Execute test operations
            IntStream.range(0, 1000).forEach(i -> 
                executor.submit(() -> {
                    long threadId = Thread.currentThread().getId();
                    monitoredThreads.add(threadId);
                    try {
                        long startOp = System.nanoTime();
                        long startCpu = threadBean.getThreadCpuTime(threadId);
                        long startUser = threadBean.getThreadUserTime(threadId);

                        // Token generation
                        long genStart = System.nanoTime();
                        String token = jwtService.generateToken(testUser);
                        long genTime = System.nanoTime() - genStart;

                        // Token validation
                        long valStart = System.nanoTime();
                        jwtService.isTokenValid(token, testUser);
                        long valTime = System.nanoTime() - valStart;

                        // Token blacklisting
                        long blackStart = System.nanoTime();
                        tokenBlacklistService.blacklist(
                            token, 
                            false, 
                            System.currentTimeMillis() + 3600000,
                            "testuser",
                            "test"
                        );
                        long blackTime = System.nanoTime() - blackStart;

                        // Record timing
                        long endCpu = threadBean.getThreadCpuTime(threadId);
                        long endUser = threadBean.getThreadUserTime(threadId);
                        
                        timings.put(i, new OperationTiming(
                            System.nanoTime() - startOp,
                            genTime,
                            valTime,
                            blackTime,
                            endCpu - startCpu,
                            endUser - startUser,
                            (endCpu - startCpu) - (endUser - startUser)
                        ));
                    } finally {
                        latch.countDown();
                    }
                })
            );

            // Wait for completion
            if (!latch.await(5, TimeUnit.MINUTES)) {
                throw new TimeoutException("Test timed out");
            }

            // Calculate CPU metrics
            List<CpuSnapshot> snapshotList = new ArrayList<>(snapshots);
            long testDuration = System.currentTimeMillis() - startTime;

            double avgCpu = snapshotList.stream()
                .mapToDouble(CpuSnapshot::processCpuLoad)
                .average()
                .orElse(0.0);

            double peakCpu = snapshotList.stream()
                .mapToDouble(CpuSnapshot::processCpuLoad)
                .max()
                .orElse(0.0);

            double avgUserTime = timings.values().stream()
                .mapToLong(OperationTiming::userTime)
                .average()
                .orElse(0.0);

            double avgSystemTime = timings.values().stream()
                .mapToLong(OperationTiming::systemTime)
                .average()
                .orElse(0.0);

            long totalCpuTime = timings.values().stream()
                .mapToLong(OperationTiming::cpuTime)
                .sum();

            Collection<OperationTiming> ops = timings.values();
            long totalOpTime = ops.stream().mapToLong(OperationTiming::totalTime).sum();

            double genPercent = ops.stream().mapToLong(OperationTiming::tokenGenTime).sum() * 100.0 / totalOpTime;
            double valPercent = ops.stream().mapToLong(OperationTiming::validationTime).sum() * 100.0 / totalOpTime;
            double blacklistPercent = ops.stream().mapToLong(OperationTiming::blacklistTime).sum() * 100.0 / totalOpTime;

            CpuMetrics metrics = new CpuMetrics(
                avgCpu * 100.0,
                peakCpu * 100.0,
                avgUserTime / 1_000_000.0,  // Convert to ms
                avgSystemTime / 1_000_000.0, // Convert to ms
                totalCpuTime / (double) timings.size() / 1_000_000.0,
                totalCpuTime,
                monitoredThreads.size(),
                (double) totalCpuTime / testDuration / 1_000_000.0 // CPU efficiency
            );

            // Print results
            System.out.printf("""

                CPU Usage Analysis:
                =================
                Overall CPU Metrics:
                ------------------
                Average CPU Usage: %.2f%%
                Peak CPU Usage: %.2f%%
                Total CPU Time: %.2f sec
                Thread Count: %d
                CPU Efficiency: %.2f

                Operation Timing:
                ----------------
                Avg User Time: %.2f ms
                Avg System Time: %.2f ms
                CPU Time per Operation: %.2f ms

                Operation Breakdown:
                ------------------
                Token Generation: %.2f%%
                Token Validation: %.2f%%
                Token Blacklisting: %.2f%%
                """,
                metrics.avgCpuUsage,
                metrics.peakCpuUsage,
                metrics.totalCpuTime / 1_000_000_000.0,
                metrics.threadCount,
                metrics.cpuEfficiency,
                metrics.avgUserTime,
                metrics.avgSystemTime,
                metrics.operationCpuCost,
                genPercent,
                valPercent,
                blacklistPercent
            );

            // Assert CPU behavior
            assertThat(metrics.avgCpuUsage)
                .as("Average CPU usage should be reasonable")
                .isLessThan(80.0);

            assertThat(metrics.operationCpuCost)
                .as("CPU time per operation should be reasonable")
                .isLessThan(100.0); // Less than 100ms per operation

            assertThat(metrics.cpuEfficiency)
                .as("CPU efficiency should be reasonable")
                .isLessThan(Runtime.getRuntime().availableProcessors() * 0.8);

        } finally {
            monitor.shutdownNow();
            executor.shutdownNow();
            CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> awaitTermination(monitor)),
                CompletableFuture.runAsync(() -> awaitTermination(executor))
            ).get(5, TimeUnit.SECONDS);
        }
    }

    private double getProcessCpuLoad(OperatingSystemMXBean osBean) {
        try {
            return (double) osBean.getClass()
                .getMethod("getProcessCpuLoad")
                .invoke(osBean);
        } catch (Exception e) {
            return -1.0;
        }
    }

    private double getSystemCpuLoad(OperatingSystemMXBean osBean) {
        try {
            return (double) osBean.getClass()
                .getMethod("getSystemCpuLoad")
                .invoke(osBean);
        } catch (Exception e) {
            return -1.0;
        }
    }

    private void awaitTermination(ExecutorService executor) {
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Executor failed to terminate: " + executor);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
