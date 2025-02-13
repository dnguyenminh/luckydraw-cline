package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Reward;

@ExtendWith(MockitoExtension.class)
class RewardSelectionServiceTest {

    private RewardSelectionService rewardSelectionService;
    private Event event;
    private List<Reward> rewards;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        rewardSelectionService = new RewardSelectionService();
        rewardSelectionService.resetCaches(); // Clear caches before each test
        baseTime = LocalDateTime.of(2025, 2, 10, 12, 0);

        event = Event.builder()
                .id(1L)
                .code("TEST001")
                .name("Test Event")
                .startDate(baseTime.minusDays(1))
                .endDate(baseTime.plusDays(7))
                .isActive(true)
                .build();

        // Create rewards with atomic remaining quantities
        rewards = Arrays.asList(
            createReward(1L, "Reward 1", 4),
            createReward(2L, "Reward 2", 10)
        );
    }

    private Reward createReward(Long id, String name, int quantity) {
        return Reward.builder()
                .id(id)
                .name(name)
                .quantity(quantity)
                .remainingQuantity(quantity)
                .isActive(true)
                .build();
    }

    @Test
    void shouldDistributeRewardsWithCorrectProbability() {
        int totalSpins = 100;
        // Set<Integer> usedPositions = new HashSet<>();
        Map<String, Set<Integer>> rewardPositions = new HashMap<>();
        
        Map<String, AtomicInteger> winCounts = new HashMap<>();
        winCounts.put("Reward 1", new AtomicInteger(0));
        winCounts.put("Reward 2", new AtomicInteger(0));
        winCounts.put("No Win", new AtomicInteger(0));

        // Track positions for each reward
        rewardPositions.put("Reward 1", new HashSet<>());
        rewardPositions.put("Reward 2", new HashSet<>());

        // Simulate 100 spins
        for (int i = 0; i < totalSpins; i++) {
            Optional<Reward> result = rewardSelectionService.selectReward(
                event, rewards, totalSpins - i, Optional.empty(), "Location1"
            );
            
            result.ifPresentOrElse(
                reward -> {
                    int wins = winCounts.get(reward.getName()).incrementAndGet();
                    assertThat(wins).isLessThanOrEqualTo(reward.getQuantity());
                },
                () -> winCounts.get("No Win").incrementAndGet()
            );
        }

        // Verify results
        int reward1Wins = winCounts.get("Reward 1").get();
        int reward2Wins = winCounts.get("Reward 2").get();
        int noWins = winCounts.get("No Win").get();

        assertThat(reward1Wins).isEqualTo(4);
        assertThat(reward2Wins).isEqualTo(10);
        assertThat(noWins).isEqualTo(86);
        assertThat(reward1Wins + reward2Wins + noWins).isEqualTo(totalSpins);
    }

    @Test
    void shouldMaintainRewardRatiosWithDecreasingSpins() {
        int totalSpins = 50; // Test with smaller number to verify ratio maintenance
        
        Map<String, AtomicInteger> winCounts = new HashMap<>();
        winCounts.put("Reward 1", new AtomicInteger(0));
        winCounts.put("Reward 2", new AtomicInteger(0));
        winCounts.put("No Win", new AtomicInteger(0));

        // First half of spins
        for (int i = 0; i < totalSpins / 2; i++) {
            Optional<Reward> result = rewardSelectionService.selectReward(
                event, rewards, totalSpins - i, Optional.empty(), "Location1"
            );
            trackResult(result, winCounts);
        }

        // Record halfway counts
        int halfwayReward1Wins = winCounts.get("Reward 1").get();
        int halfwayReward2Wins = winCounts.get("Reward 2").get();

        // Complete remaining spins
        for (int i = totalSpins / 2; i < totalSpins; i++) {
            Optional<Reward> result = rewardSelectionService.selectReward(
                event, rewards, totalSpins - i, Optional.empty(), "Location1"
            );
            trackResult(result, winCounts);
        }

        // Final counts
        int finalReward1Wins = winCounts.get("Reward 1").get();
        int finalReward2Wins = winCounts.get("Reward 2").get();

        // Verify distribution pattern
        assertThat(halfwayReward1Wins).isLessThanOrEqualTo(2); // ~half of total reward 1
        assertThat(halfwayReward2Wins).isLessThanOrEqualTo(5); // ~half of total reward 2
        assertThat(finalReward1Wins).isEqualTo(4);
        assertThat(finalReward2Wins).isEqualTo(10);
    }

    @Test
    void shouldHandleConcurrentAccessWithoutPositionReuse() throws InterruptedException {
        int numberOfThreads = 10;
        int spinsPerThread = 20;
        int totalSpins = numberOfThreads * spinsPerThread;
        
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);
        
        // Set<Integer> usedPositions = Collections.synchronizedSet(new HashSet<>());
        Map<String, AtomicInteger> winCounts = new HashMap<>();
        winCounts.put("Reward 1", new AtomicInteger(0));
        winCounts.put("Reward 2", new AtomicInteger(0));
        winCounts.put("No Win", new AtomicInteger(0));

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < spinsPerThread; j++) {
                        Optional<Reward> result = rewardSelectionService.selectReward(
                            event, rewards, totalSpins, Optional.empty(), "Location1"
                        );
                        trackResult(result, winCounts);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = completionLatch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();
        
        assertThat(completed).isTrue();
        
        int reward1Wins = winCounts.get("Reward 1").get();
        int reward2Wins = winCounts.get("Reward 2").get();
        
        assertThat(reward1Wins).isLessThanOrEqualTo(4);
        assertThat(reward2Wins).isLessThanOrEqualTo(10);
        assertThat(reward1Wins + reward2Wins)
            .as("Total wins should not exceed available rewards")
            .isLessThanOrEqualTo(14);
    }

    private void trackResult(Optional<Reward> result, Map<String, AtomicInteger> winCounts) {
        result.ifPresentOrElse(
            reward -> winCounts.get(reward.getName()).incrementAndGet(),
            () -> winCounts.get("No Win").incrementAndGet()
        );
    }
}