package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.persistence.EntityManager;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private EntityManager entityManager;

    private Event event;
    private Reward reward;

    @BeforeEach
    @Transactional
    void setUp() {
        // Save the event
        event = Event.builder()
                .code("TEST-EVENT-" + System.currentTimeMillis())
                .name("Test Event")
                .totalSpins(1000L)
                .remainingSpins(1000L)
                .isActive(true)
                .build();
        event = eventRepository.save(event);

        // Save the reward
        reward = Reward.builder()
                .event(event)
                .name("Test Reward")
                .quantity(100)
                .remainingQuantity(100)
                .probability(1.0)
                .isActive(true)
                .build();
        reward = rewardRepository.saveAndFlush(reward);

        // Clear the entity manager to ensure no cached state
        entityManager.clear();
    }

    @Test
    public void concurrentRewardDecrements_ShouldNotOverallocate() throws InterruptedException {

        int threadCount = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        rewardService.decrementRemainingQuantity(reward.getId());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS); // Wait for all threads to finish
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS); // Wait for up to 30 seconds for all tasks to complete

        Reward updatedReward = rewardRepository.findById(reward.getId()).get();
        Assertions.assertEquals(0, updatedReward.getRemainingQuantity().longValue());
    }
}
