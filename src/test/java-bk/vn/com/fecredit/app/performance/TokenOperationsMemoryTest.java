package vn.com.fecredit.app.performance;

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
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TokenOperationsMemoryTest {

    @Autowired private JwtService jwtService;
    @Autowired private UserService userService;
    @Autowired private TokenBlacklistService tokenBlacklistService;

    private record MemorySnapshot(
        long timestamp,
        long heapUsed,
        long heapMax,
        long nonHeapUsed,
        long youngGenUsed,
        long oldGenUsed,
        long metaspaceUsed,
        int objectsPending
    ) {}

    private record MemoryMetrics(
        double avgHeapUsage,
        double maxHeapUsage,
        double allocRatePerSec,
        double avgAllocationPerOp,
        long gcPauseTotal,
        int gcCount,
        double avgObjectsPending,
        double metaspaceGrowth
    ) {}

    @Test
    public void shouldMonitorMemoryUsage() throws Exception {
        // Given
        User testUser = (User) userService.loadUserByUsername("testuser");
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(1000);
        ConcurrentLinkedQueue<MemorySnapshot> snapshots = new ConcurrentLinkedQueue<>();
        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();

        // Memory monitoring setup
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

        Map<String, Long> initialGcCounts = new HashMap<>();
        Map<String, Long> initialGcTimes = new HashMap<>();
        gcBeans.forEach(gc -> {
            initialGcCounts.put(gc.getName(), gc.getCollectionCount());
            initialGcTimes.put(gc.getName(), gc.getCollectionTime());
        });

        long startTime = System.currentTimeMillis();

        try {
            // Start memory monitoring
            monitor.scheduleAtFixedRate(() -> {
                MemoryUsage heap = memoryBean.getHeapMemoryUsage();
                MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
                
                long youngGen = pools.stream()
                    .filter(p -> p.getName().contains("Eden") || p.getName().contains("Survivor"))
                    .mapToLong(p -> p.getUsage().getUsed())
                    .sum();
                
                long oldGen = pools.stream()
                    .filter(p -> p.getName().contains("Old") || p.getName().contains("Tenured"))
                    .mapToLong(p -> p.getUsage().getUsed())
                    .sum();
                
                long metaspace = pools.stream()
                    .filter(p -> p.getName().contains("Metaspace"))
                    .mapToLong(p -> p.getUsage().getUsed())
                    .sum();

                snapshots.add(new MemorySnapshot(
                    System.nanoTime(),
                    heap.getUsed(),
                    heap.getMax(),
                    nonHeap.getUsed(),
                    youngGen,
                    oldGen,
                    metaspace,
                    memoryBean.getObjectPendingFinalizationCount()
                ));
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

            // Calculate memory metrics
            List<MemorySnapshot> snapshotList = new ArrayList<>(snapshots);
            long testDuration = System.currentTimeMillis() - startTime;
            
            // Calculate GC activity
            long totalGcCount = gcBeans.stream()
                .mapToLong(gc -> gc.getCollectionCount() - initialGcCounts.get(gc.getName()))
                .sum();
            
            long totalGcTime = gcBeans.stream()
                .mapToLong(gc -> gc.getCollectionTime() - initialGcTimes.get(gc.getName()))
                .sum();

            double avgHeap = snapshotList.stream()
                .mapToLong(MemorySnapshot::heapUsed)
                .average()
                .orElse(0.0);

            double maxHeap = snapshotList.stream()
                .mapToLong(MemorySnapshot::heapUsed)
                .max()
                .orElse(0);

            // Calculate allocation rate (bytes/sec)
            double allocRate = (snapshotList.get(snapshotList.size() - 1).heapUsed() - 
                              snapshotList.get(0).heapUsed()) * 1000.0 / testDuration;

            double avgAllocation = allocRate / (1000.0 / testDuration);

            double avgPending = snapshotList.stream()
                .mapToInt(MemorySnapshot::objectsPending)
                .average()
                .orElse(0.0);

            double metaspaceGrowth = snapshotList.get(snapshotList.size() - 1).metaspaceUsed() -
                                   snapshotList.get(0).metaspaceUsed();

            MemoryMetrics metrics = new MemoryMetrics(
                avgHeap,
                maxHeap,
                allocRate,
                avgAllocation,
                totalGcTime,
                (int) totalGcCount,
                avgPending,
                metaspaceGrowth
            );

            // Print results
            System.out.printf("""

                Memory Usage Analysis:
                ====================
                Heap Memory:
                ------------
                Average Heap Usage: %.2f MB
                Maximum Heap Usage: %.2f MB
                Allocation Rate: %.2f MB/sec
                Average Allocation per Operation: %.2f KB

                Garbage Collection:
                ------------------
                Total GC Pause Time: %d ms
                Number of Collections: %d
                Average GC Pause: %.2f ms
                GC Time Percentage: %.2f%%

                Object Management:
                -----------------
                Average Pending Finalizers: %.2f
                Metaspace Growth: %.2f MB
                Young:Old Gen Ratio: %.2f
                """,
                metrics.avgHeapUsage / (1024.0 * 1024.0),
                metrics.maxHeapUsage / (1024.0 * 1024.0),
                metrics.allocRatePerSec / (1024.0 * 1024.0),
                metrics.avgAllocationPerOp / 1024.0,
                metrics.gcPauseTotal,
                metrics.gcCount,
                metrics.gcCount == 0 ? 0 : metrics.gcPauseTotal / (double) metrics.gcCount,
                metrics.gcPauseTotal * 100.0 / testDuration,
                metrics.avgObjectsPending,
                metrics.metaspaceGrowth / (1024.0 * 1024.0),
                calculateYoungOldRatio(snapshotList)
            );

            // Assert memory behavior
            assertThat(metrics.avgHeapUsage)
                .as("Average heap usage should be reasonable")
                .isLessThan(Runtime.getRuntime().maxMemory() * 0.8);

            assertThat(metrics.gcPauseTotal)
                .as("GC pause time should be minimal")
                .isLessThan(testDuration / 10); // Less than 10% of test time

            assertThat(metrics.avgAllocationPerOp)
                .as("Allocation per operation should be reasonable")
                .isLessThan(50 * 1024); // Less than 50KB per operation

            assertThat(metrics.avgObjectsPending)
                .as("Should not accumulate pending finalizers")
                .isLessThan(100);

        } finally {
            monitor.shutdownNow();
            executor.shutdownNow();
            CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> awaitTermination(monitor)),
                CompletableFuture.runAsync(() -> awaitTermination(executor))
            ).get(5, TimeUnit.SECONDS);
        }
    }

    private double calculateYoungOldRatio(List<MemorySnapshot> snapshots) {
        double avgYoung = snapshots.stream()
            .mapToLong(MemorySnapshot::youngGenUsed)
            .average()
            .orElse(0.0);
        
        double avgOld = snapshots.stream()
            .mapToLong(MemorySnapshot::oldGenUsed)
            .average()
            .orElse(1.0); // Avoid division by zero
            
        return avgYoung / avgOld;
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
