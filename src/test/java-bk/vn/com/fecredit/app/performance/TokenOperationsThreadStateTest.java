package vn.com.fecredit.app.performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
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
public class TokenOperationsThreadStateTest {

    @Autowired private JwtService jwtService;
    @Autowired private UserService userService;
    @Autowired private TokenBlacklistService tokenBlacklistService;

    private record ThreadSnapshot(
        long timestamp,
        Thread.State state,
        long cpuTime,
        long userTime,
        long blockedTime,
        long blockedCount,
        long waitedTime,
        long waitedCount
    ) {}

    private record ThreadMetrics(
        double runningPercentage,
        double blockedPercentage,
        double waitingPercentage,
        double avgBlockedTime,
        double avgWaitTime,
        long maxBlockedTime,
        long maxWaitTime,
        long totalStateTransitions
    ) {}

    @Test
    public void shouldMonitorThreadStates() throws Exception {
        // Given
        User testUser = (User) userService.loadUserByUsername("testuser");
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(1000);
        ConcurrentHashMap<Long, List<ThreadSnapshot>> threadHistory = new ConcurrentHashMap<>();
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        threadBean.setThreadContentionMonitoringEnabled(true);

        try {
            // Start thread state monitoring
            monitor.scheduleAtFixedRate(() -> {
                ThreadInfo[] threads = threadBean.dumpAllThreads(true, true);
                long now = System.nanoTime();

                for (ThreadInfo info : threads) {
                    if (info.getThreadName().startsWith("pool-")) {
                        threadHistory.computeIfAbsent(info.getThreadId(), k -> new CopyOnWriteArrayList<>())
                            .add(new ThreadSnapshot(
                                now,
                                info.getThreadState(),
                                threadBean.getThreadCpuTime(info.getThreadId()),
                                threadBean.getThreadUserTime(info.getThreadId()),
                                info.getBlockedTime(),
                                info.getBlockedCount(),
                                info.getWaitedTime(),
                                info.getWaitedCount()
                            ));
                    }
                }
            }, 0, 100, TimeUnit.MILLISECONDS);

            // Execute test operations
            IntStream.range(0, 1000).forEach(i -> 
                executor.submit(() -> {
                    try {
                        String token = jwtService.generateToken(testUser);
                        jwtService.isTokenValid(token, testUser);
                        tokenBlacklistService.blacklist(
                            token, 
                            false, 
                            System.currentTimeMillis() + 3600000,
                            "testuser",
                            "test"
                        );
                    } finally {
                        latch.countDown();
                    }
                })
            );

            // Wait for completion
            if (!latch.await(5, TimeUnit.MINUTES)) {
                throw new TimeoutException("Test timed out");
            }

            // Calculate thread metrics
            Map<Thread.State, Long> stateCounts = threadHistory.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(
                    ThreadSnapshot::state,
                    Collectors.counting()
                ));

            long totalSnapshots = stateCounts.values().stream().mapToLong(l -> l).sum();
            
            ThreadMetrics metrics = new ThreadMetrics(
                percentage(stateCounts.getOrDefault(Thread.State.RUNNABLE, 0L), totalSnapshots),
                percentage(stateCounts.getOrDefault(Thread.State.BLOCKED, 0L), totalSnapshots),
                percentage(stateCounts.getOrDefault(Thread.State.WAITING, 0L) + 
                         stateCounts.getOrDefault(Thread.State.TIMED_WAITING, 0L), totalSnapshots),
                threadHistory.values().stream()
                    .flatMap(List::stream)
                    .mapToLong(ThreadSnapshot::blockedTime)
                    .average()
                    .orElse(0.0),
                threadHistory.values().stream()
                    .flatMap(List::stream)
                    .mapToLong(ThreadSnapshot::waitedTime)
                    .average()
                    .orElse(0.0),
                threadHistory.values().stream()
                    .flatMap(List::stream)
                    .mapToLong(ThreadSnapshot::blockedTime)
                    .max()
                    .orElse(0L),
                threadHistory.values().stream()
                    .flatMap(List::stream)
                    .mapToLong(ThreadSnapshot::waitedTime)
                    .max()
                    .orElse(0L),
                threadHistory.values().stream()
                    .flatMap(List::stream)
                    .mapToLong(s -> s.blockedCount + s.waitedCount)
                    .sum()
            );

            // Print results
            System.out.printf("""

                Thread State Analysis:
                ====================
                Thread State Distribution:
                ------------------------
                Running: %.2f%%
                Blocked: %.2f%%
                Waiting: %.2f%%

                Contention Metrics:
                -----------------
                Average Blocked Time: %.2f ms
                Average Wait Time: %.2f ms
                Maximum Blocked Time: %d ms
                Maximum Wait Time: %d ms
                Total State Transitions: %d

                Thread Snapshots:
                ---------------
                Total Threads Monitored: %d
                Total Snapshots: %d
                Snapshot Period: 100ms
                """,
                metrics.runningPercentage,
                metrics.blockedPercentage,
                metrics.waitingPercentage,
                metrics.avgBlockedTime,
                metrics.avgWaitTime,
                metrics.maxBlockedTime,
                metrics.maxWaitTime,
                metrics.totalStateTransitions,
                threadHistory.size(),
                totalSnapshots
            );

            // Assert thread behavior
            assertThat(metrics.runningPercentage)
                .as("Threads should be mostly running")
                .isGreaterThan(60.0);

            assertThat(metrics.blockedPercentage)
                .as("Blocked time should be minimal")
                .isLessThan(20.0);

            assertThat(metrics.avgBlockedTime)
                .as("Average blocked time should be reasonable")
                .isLessThan(100.0);

            assertThat(metrics.totalStateTransitions)
                .as("Should see reasonable thread state transitions")
                .isGreaterThan(0L)
                .isLessThan(totalSnapshots * 2);

        } finally {
            monitor.shutdownNow();
            executor.shutdownNow();
            CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> awaitTermination(monitor)),
                CompletableFuture.runAsync(() -> awaitTermination(executor))
            ).get(5, TimeUnit.SECONDS);
        }
    }

    private double percentage(long count, long total) {
        return total == 0 ? 0.0 : (count * 100.0) / total;
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
