package vn.com.fecredit.app.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Reward;

class RewardSelectionServicePerformanceTest {

    private RewardSelectionService service;
    private Event event;
    private List<Reward> rewards;
    private static final int REWARD_COUNT = 100;
    private static final double PROBABILITY_PER_REWARD = 1.0 / REWARD_COUNT;

    @BeforeEach
    void setUp() {
        service = new RewardSelectionService();
        event = Event.builder()
                .id(1L)
                .isActive(true)
                .build();
        rewards = createTestRewards();
    }

    private List<Reward> createTestRewards() {
        List<Reward> rewardList = new ArrayList<>();
        for (int i = 0; i < REWARD_COUNT; i++) {
            rewardList.add(Reward.builder()
                    .id((long) i)
                    .probability(PROBABILITY_PER_REWARD)
                    .remainingQuantity(1000)
                    .isActive(true)
                    .build());
        }
        return rewardList;
    }

    @Test
    void shouldMaintainProbabilityDistributionUnderLoad() throws InterruptedException {
        int threadCount = 10;
        int iterationsPerThread = 10000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        
        Map<Long, AtomicInteger> rewardCounts = new ConcurrentHashMap<>();
        AtomicInteger totalCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Create threads
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    for (int j = 0; j < iterationsPerThread; j++) {
                        Reward selected = service.selectReward(
                            event,
                            rewards,
                            Thread.currentThread().getId(),
                            System.currentTimeMillis(),
                            "TEST"
                        );
                        if (selected != null) {
                            rewardCounts.computeIfAbsent(selected.getId(), k -> new AtomicInteger())
                                    .incrementAndGet();
                            totalCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // Start timing
        Instant start = Instant.now();
        startLatch.countDown(); // Start all threads simultaneously
        
        // Wait for completion
        assertTrue(completionLatch.await(30, TimeUnit.SECONDS), "Test took too long to complete");
        Duration duration = Duration.between(start, Instant.now());

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // Verify results
        assertEquals(0, errorCount.get(), "Should have no errors");
        assertEquals(threadCount * iterationsPerThread, totalCount.get(), "All selections should succeed");

        // Check probability distribution
        int expectedCount = totalCount.get() / REWARD_COUNT;
        double allowedDeviation = 0.1; // Allow 10% deviation from expected

        for (Reward reward : rewards) {
            AtomicInteger count = rewardCounts.get(reward.getId());
            assertNotNull(count, "Every reward should be selected at least once");
            
            int actualCount = count.get();
            double ratio = (double) actualCount / totalCount.get();
            double expectedRatio = PROBABILITY_PER_REWARD;
            
            assertTrue(Math.abs(ratio - expectedRatio) < allowedDeviation,
                    String.format("Reward %d ratio (%.3f) should be close to expected (%.3f)",
                            reward.getId(), ratio, expectedRatio));
        }

        // Performance metrics
        long totalOperations = threadCount * iterationsPerThread;
        double operationsPerSecond = totalOperations / (duration.toMillis() / 1000.0);
        
        System.out.printf("Performance Test Results:%n");
        System.out.printf("Total operations: %d%n", totalOperations);
        System.out.printf("Duration: %dms%n", duration.toMillis());
        System.out.printf("Throughput: %.2f ops/sec%n", operationsPerSecond);
        
        assertTrue(operationsPerSecond > 10000, 
                String.format("Performance too low: %.2f ops/sec", operationsPerSecond));
    }

    @Test
    void shouldHandleRewardDepletion() throws InterruptedException {
        // Create rewards with limited quantity
        List<Reward> limitedRewards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            limitedRewards.add(Reward.builder()
                    .id((long) i)
                    .probability(0.2)
                    .remainingQuantity(10)
                    .isActive(true)
                    .build());
        }

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Create threads
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        Reward selected = service.selectReward(
                            event,
                            limitedRewards,
                            Thread.currentThread().getId(),
                            System.currentTimeMillis(),
                            "TEST"
                        );
                        if (selected != null) {
                            successCount.incrementAndGet();
                            selected.setRemainingQuantity(selected.getRemainingQuantity() - 1);
                        }
                    }
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        assertTrue(completionLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // Verify that we got exactly the number of rewards available
        assertEquals(50, successCount.get(), "Should select exactly the available quantity");
    }
}