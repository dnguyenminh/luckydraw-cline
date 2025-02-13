package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.mapper.RewardMapper;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;

@ExtendWith(MockitoExtension.class)
class RewardConcurrencyTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SpinHistoryRepository spinHistoryRepository;

    @Mock
    private GoldenHourService goldenHourService;

    @Mock
    private GoldenHourRepository goldenHourRepository;

    private GoldenHourMapper goldenHourMapper;
    private RewardMapper rewardMapper;
    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        goldenHourMapper = new GoldenHourMapper();
        rewardMapper = new RewardMapper(goldenHourMapper);
        rewardService = new RewardService(
            rewardRepository,
            eventRepository,
            goldenHourRepository,
            rewardMapper,
            goldenHourMapper
        );
    }

    @Test
    void updateQuantity_ShouldHandleConcurrentUpdates() throws InterruptedException {
        // Given
        Long rewardId = 1L;
        Reward reward = Reward.builder()
                .id(rewardId)
                .quantity(100)
                .remainingQuantity(50)
                .version(0L)
                .build();

        // First call succeeds
        when(rewardRepository.findById(rewardId))
                .thenReturn(Optional.of(reward));
        
        // Second call throws OptimisticLockingFailureException once, then succeeds
        when(rewardRepository.save(any(Reward.class)))
                .thenThrow(OptimisticLockingFailureException.class)
                .thenReturn(reward);

        // When & Then
        // The first update should succeed after retry
        rewardService.updateQuantity(rewardId, 200);
        
        verify(rewardRepository, times(2)).save(any(Reward.class));
    }

    @Test
    void updateQuantity_ShouldFailAfterMaxRetries() {
        // Given
        Long rewardId = 1L;
        Reward reward = Reward.builder()
                .id(rewardId)
                .quantity(100)
                .remainingQuantity(50)
                .version(0L)
                .build();

        when(rewardRepository.findById(rewardId))
                .thenReturn(Optional.of(reward));

        // Simulate persistent concurrent modifications
        when(rewardRepository.save(any(Reward.class)))
                .thenThrow(OptimisticLockingFailureException.class);

        // When & Then
        assertThatThrownBy(() -> rewardService.updateQuantity(rewardId, 200))
                .isInstanceOf(OptimisticLockingFailureException.class)
                .hasMessageContaining("Failed to update reward quantity after");

        verify(rewardRepository, times(3)).save(any(Reward.class));
    }

    @Test
    void updateQuantity_ShouldHandleMultipleConcurrentUpdates() throws InterruptedException {
        // Given
        int threadCount = 5;
        Long rewardId = 1L;
        Reward reward = Reward.builder()
                .id(rewardId)
                .quantity(100)
                .remainingQuantity(50)
                .version(0L)
                .build();

        when(rewardRepository.findById(rewardId))
                .thenReturn(Optional.of(reward));

        // Mock save to simulate some concurrent updates succeeding and others failing
        when(rewardRepository.save(any(Reward.class)))
                .thenThrow(OptimisticLockingFailureException.class) // First attempt fails
                .thenReturn(reward) // Second attempt succeeds
                .thenThrow(OptimisticLockingFailureException.class) // Third attempt fails
                .thenReturn(reward) // Fourth attempt succeeds
                .thenReturn(reward); // Fifth attempt succeeds directly

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            final int quantity = 200 + i * 50; // Different quantity for each thread
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    rewardService.updateQuantity(rewardId, quantity);
                } catch (Exception e) {
                    // Exception expected for some threads
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all threads to complete
        completionLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        // Verify that save was called multiple times due to retries
        verify(rewardRepository, atLeast(threadCount)).save(any(Reward.class));
    }
}
