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
public class TokenOperationsConcurrencyTest {

    @Autowired private JwtService jwtService;
    @Autowired private UserService userService;
    @Autowired private TokenBlacklistService tokenBlacklistService;

    private record OperationResult(
        long duration,
        long cpuTime,
        Throwable error
    ) {}

    private record PerformanceMetrics(
        long p50,
        long p90,
        long p95,
        long p99,
        double average,
        double throughput,
        long totalGcCount,
        long totalGcTime
    ) {}

    @Test
    public void shouldHandleConcurrentOperations() throws Exception {
        // Given
        User testUser = (User) userService.loadUserByUsername("testuser");
        ExecutorService executor = Executors.newFixedThreadPool(20);
        ConcurrentHashMap<Integer, OperationResult> results = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(1000);
        long startTime = System.currentTimeMillis();

        // Monitoring setup
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        threadBean.setThreadCpuTimeEnabled(true);
        
        long initialMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long initialCpuTime = threadBean.getCurrentThreadCpuTime();
        Map<String, Long> initialGcCounts = new HashMap<>();
        Map<String, Long> initialGcTimes = new HashMap<>();
        
        // Capture initial GC stats
        gcBeans.forEach(gc -> {
            initialGcCounts.put(gc.getName(), gc.getCollectionCount());
            initialGcTimes.put(gc.getName(), gc.getCollectionTime());
        });

        try {
            // When - execute concurrent operations
            IntStream.range(0, 1000).forEach(i -> 
                executor.submit(() -> {
                    long threadStartCpu = threadBean.getCurrentThreadCpuTime();
                    long opStart = System.nanoTime();
                    
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
                        
                        results.put(i, new OperationResult(
                            System.nanoTime() - opStart,
                            threadBean.getCurrentThreadCpuTime() - threadStartCpu,
                            null
                        ));
                    } catch (Throwable e) {
                        results.put(i, new OperationResult(
                            System.nanoTime() - opStart,
                            threadBean.getCurrentThreadCpuTime() - threadStartCpu,
                            e
                        ));
                    } finally {
                        latch.countDown();
                    }
                })
            );

            // Then - verify timing and completion
            if (!latch.await(5, TimeUnit.MINUTES)) {
                throw new TimeoutException("Test timed out waiting for operations to complete");
            }

            // Calculate metrics
            long totalTime = System.currentTimeMillis() - startTime;
            long finalMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long finalCpuTime = threadBean.getCurrentThreadCpuTime();

            // Calculate percentiles
            List<Long> durations = results.values().stream()
                .mapToLong(r -> r.duration)
                .sorted()
                .boxed()
                .collect(Collectors.toList());

            int size = durations.size();
            PerformanceMetrics metrics = new PerformanceMetrics(
                durations.get((int)(size * 0.5)) / 1_000_000,  // p50 in ms
                durations.get((int)(size * 0.9)) / 1_000_000,  // p90 in ms
                durations.get((int)(size * 0.95)) / 1_000_000, // p95 in ms
                durations.get((int)(size * 0.99)) / 1_000_000, // p99 in ms
                durations.stream().mapToLong(d -> d).average().orElse(0.0) / 1_000_000.0,
                1000 * 1000.0 / totalTime,
                gcBeans.stream().mapToLong(gc -> gc.getCollectionCount() - initialGcCounts.get(gc.getName())).sum(),
                gcBeans.stream().mapToLong(gc -> gc.getCollectionTime() - initialGcTimes.get(gc.getName())).sum()
            );
            
            // Resource calculations
            long memoryDelta = finalMemory - initialMemory;
            long memoryPerOp = memoryDelta / 1000;
            double cpuTimeSeconds = (finalCpuTime - initialCpuTime) / 1_000_000_000.0;
            
            // Error analysis
            long errorCount = results.values().stream()
                .filter(r -> r.error != null)
                .count();

            Map<String, Long> errorTypes = results.values().stream()
                .filter(r -> r.error != null)
                .map(r -> r.error.getClass().getSimpleName())
                .collect(Collectors.groupingBy(
                    type -> type,
                    HashMap::new,
                    Collectors.counting()
                ));

            // Print results
            System.out.printf("""

                Concurrent Operations Test Results:
                ================================
                Performance Metrics:
                ------------------
                Total Operations: %d
                Total Time: %d ms
                Average Time: %.2f ms
                P50 Time: %d ms
                P90 Time: %d ms
                P95 Time: %d ms
                P99 Time: %d ms
                Throughput: %.2f ops/sec

                Resource Usage:
                --------------
                Initial Heap: %.2f MB
                Final Heap: %.2f MB
                Memory Delta: %.2f MB
                Memory per Operation: %d KB
                Total CPU Time: %.2f sec
                CPU Time per Operation: %.2f ms
                GC Collections: %d
                GC Total Time: %d ms

                Error Distribution:
                -----------------
                Total Errors: %d
                Error Rate: %.2f%%
                Error Types: %s
                """,
                results.size(),
                totalTime,
                metrics.average,
                metrics.p50,
                metrics.p90,
                metrics.p95,
                metrics.p99,
                metrics.throughput,
                initialMemory / (1024.0 * 1024.0),
                finalMemory / (1024.0 * 1024.0),
                memoryDelta / (1024.0 * 1024.0),
                memoryPerOp / 1024,
                cpuTimeSeconds,
                cpuTimeSeconds * 1000.0 / results.size(),
                metrics.totalGcCount,
                metrics.totalGcTime,
                errorCount,
                errorCount * 100.0 / results.size(),
                errorTypes
            );

            // Assert performance criteria
            assertThat(metrics.p95)
                .as("95th percentile should be reasonable")
                .isLessThan(1000L);
            
            assertThat(metrics.throughput)
                .as("Should maintain minimum throughput")
                .isGreaterThan(10.0);
            
            assertThat(memoryPerOp)
                .as("Memory usage per operation should be reasonable")
                .isLessThan(10L * 1024L);

            assertThat(metrics.totalGcTime)
                .as("GC time should be minimal")
                .isLessThan(totalTime / 10); // Less than 10% of total time

            assertThat(errorCount)
                .as("Error rate should be minimal")
                .isLessThan(10L);

        } finally {
            executor.shutdownNow();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Executor service failed to terminate");
            }
        }
    }
}
