package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
class RewardConcurrencyTest {

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RewardService rewardService;

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
    }

    @Test
    @Transactional
    void concurrentRewardDecrements_ShouldNotOverallocate() throws InterruptedException {
        // Given
        final Reward reward = rewardRepository.save(Reward.builder()
            .event(event)
            .name("Test Reward")
            .quantity(100)
            .remainingQuantity(100)
            .probability(1.0)
            .isActive(true)
            .build());

        int numThreads = 10;
        int numAttemptsPerThread = 15;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        List<Boolean> results = new ArrayList<>();

        // When
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < numAttemptsPerThread; j++) {
                        if (rewardService.decrementRemainingQuantity(reward.getId())) {
                            results.add(true);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Then
        Reward finalReward = rewardRepository.findById(reward.getId()).orElseThrow();
        assertThat(finalReward.getRemainingQuantity()).isEqualTo(0);
        assertThat(results).hasSize(100); // Initial quantity
    }
}
