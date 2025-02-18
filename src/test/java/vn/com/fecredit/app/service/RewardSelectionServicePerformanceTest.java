package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Reward;

@DisplayName("Reward Selection Performance Tests")
class RewardSelectionServicePerformanceTest {

    private RewardSelectionService rewardSelectionService;
    private Event event;
    private List<Reward> rewards;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        rewardSelectionService = new RewardSelectionService();
        baseTime = LocalDateTime.now();

        event = Event.builder()
                .id(1L)
                .code("PERF-TEST")
                .name("Performance Test Event")
                .startDate(baseTime)
                .endDate(baseTime.plusDays(7))
                .isActive(true)
                .build();

        // Set up rewards with total equal to test iterations
        rewards = Arrays.asList(
            createReward(1L, "Northern Reward", 4000, "HANOI,HAIPHONG,NAMDINH"),  // 40%
            createReward(2L, "Southern Reward", 3000, "HOCHIMINH,CANTHO,VUNGTAU"), // 30%
            createReward(3L, "National Reward", 3000, "")  // 30%
        );
    }

    private Reward createReward(Long id, String name, int quantity, String provinces) {
        return Reward.builder()
                .id(id)
                .name(name)
                .quantity(quantity)
                .remainingQuantity(quantity)
                .applicableProvinces(provinces)
                .isActive(true)
                .build();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("Should handle large number of spins efficiently")
    void shouldHandleLargeNumberOfSpinsEfficiently() {
        // Test parameters
        int testIterations = 10_000;   // Total iterations matches total rewards
        int warmupIterations = 1_000;  // Warm up iterations
        String testLocation = "HANOI";  // Test with northern location

        // Track remaining spins
        AtomicLong remainingSpins = new AtomicLong(testIterations);

        // Warmup phase (results ignored)
        for (int i = 0; i < warmupIterations && remainingSpins.get() > 0; i++) {
            rewardSelectionService.selectReward(
                event, rewards, remainingSpins.get(), Optional.empty(), testLocation
            );
            remainingSpins.decrementAndGet();
        }

        // Reset for test phase
        remainingSpins.set(testIterations);
        rewardSelectionService.resetCaches();
        resetRewardQuantities();
        System.gc();

        // Test phase
        Map<String, Integer> winCounts = new HashMap<>();
        long startTime = System.nanoTime();

        // Main test loop
        while (remainingSpins.get() > 0) {
            long currentSpins = remainingSpins.get();
            Optional<Reward> result = rewardSelectionService.selectReward(
                event, rewards, currentSpins, Optional.empty(), testLocation
            );

            if (result.isPresent()) {
                winCounts.merge(result.get().getName(), 1, Integer::sum);
            }
            remainingSpins.decrementAndGet();
        }

        long duration = System.nanoTime() - startTime;
        double milliseconds = duration / 1_000_000.0;
        double microsPerOp = (duration / 1000.0) / testIterations;
        double selectionsPerSecond = (testIterations * 1000.0) / milliseconds;

        int totalWins = winCounts.values().stream().mapToInt(Integer::intValue).sum();
        double overallWinRate = (totalWins * 100.0) / testIterations;

        System.out.printf("""
            Performance Results:
            Total Time: %.2f ms
            Avg Time per Selection: %.3f µs
            Selections per Second: %.2f
            Overall Win Rate: %.1f%%
            
            Win Distribution:
            %s
            
            Remaining Quantities:
            %s
            """,
            milliseconds,
            microsPerOp,
            selectionsPerSecond,
            overallWinRate,
            formatWinCounts(winCounts, testIterations),
            formatRemainingQuantities()
        );

        // Verify location filtering
        assertThat(winCounts)
            .containsKey("Northern Reward")
            .containsKey("National Reward")
            .doesNotContainKey("Southern Reward");

        // Performance requirement
        assertThat(selectionsPerSecond)
            .as("Should process at least 10K selections/second")
            .isGreaterThan(10_000.0);

        // Since total rewards match iterations, all rewards should be claimed
        assertThat(totalWins)
            .as("All available rewards should be claimed")
            .isEqualTo(7000); // 4000 Northern + 3000 National available for HANOI
    }

    private void resetRewardQuantities() {
        rewards.forEach(r -> r.setRemainingQuantity(r.getQuantity()));
    }

    private String formatWinCounts(Map<String, Integer> winCounts, int total) {
        StringBuilder sb = new StringBuilder();
        winCounts.forEach((name, count) -> {
            double percentage = (count * 100.0) / total;
            sb.append(String.format("  %s: %d (%.1f%%)\n", name, count, percentage));
        });
        return sb.toString();
    }

    private String formatRemainingQuantities() {
        StringBuilder sb = new StringBuilder();
        rewards.forEach(r -> 
            sb.append(String.format("  %s: %d/%d\n", 
                r.getName(), r.getRemainingQuantity(), r.getQuantity()))
        );
        return sb.toString();
    }
}