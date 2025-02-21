package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.RewardRepository;

@SpringBootTest
@ActiveProfiles("test")
class RewardSelectionServicePerformanceTest {

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RewardSelectionService rewardSelectionService;

    private Event event;

    @BeforeEach
    void setUp() {
        event = Event.builder()
            .code("TEST-EVENT-" + System.currentTimeMillis())
            .name("Test Event")
            .totalSpins(1000L)
            .remainingSpins(1000L)
            .isActive(true)
            .build();
        event = eventRepository.save(event);

        // Create test rewards
        List<Reward> rewards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Reward reward = Reward.builder()
                .event(event)
                .name("Test Reward " + i)
                .quantity(100)
                .remainingQuantity(100)
                .probability(0.2)
                .isActive(true)
                .build();
            reward.setApplicableProvincesFromString("HN,HCM");
            rewards.add(reward);
        }
        rewardRepository.saveAll(rewards);
    }

    @Test
    @Transactional
    void concurrentRewardSelection_ShouldNotOverallocate() throws InterruptedException {
        int numThreads = 10;
        int numAttemptsPerThread = 15;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        List<Reward> selectedRewards = new ArrayList<>();

        // When
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < numAttemptsPerThread; j++) {
                        Optional<Reward> reward = rewardSelectionService.selectReward(event.getId(), "HN");
                        reward.ifPresent(selectedRewards::add);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Then
        int totalRewardsSelected = selectedRewards.size();
        assertThat(totalRewardsSelected).isLessThanOrEqualTo(500); // Total available rewards
        
        // Verify remaining quantities
        List<Reward> finalRewards = rewardRepository.findByEventId(event.getId());
        for (Reward reward : finalRewards) {
            assertThat(reward.getRemainingQuantity()).isGreaterThanOrEqualTo(0);
            long selectedCount = selectedRewards.stream()
                .filter(r -> r.getId().equals(reward.getId()))
                .count();
            assertThat(selectedCount).isLessThanOrEqualTo(reward.getQuantity());
        }
    }
}