package vn.com.fecredit.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.dto.SpinRequest;
import vn.com.fecredit.app.dto.SpinResultDTO;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;

@ExtendWith(MockitoExtension.class)
public class SpinServiceConcurrencyTest {

    @Mock private EventRepository eventRepository;
    @Mock private ParticipantRepository participantRepository;
    @Mock private RewardRepository rewardRepository;
    @Mock private SpinHistoryRepository spinHistoryRepository;
    @Mock private GoldenHourRepository goldenHourRepository;
    @Mock private RewardSelectionService rewardSelectionService;

    private SpinService spinService;

    @BeforeEach
    void setUp() {
        spinService = new SpinService(
            eventRepository,
            participantRepository,
            rewardRepository,
            spinHistoryRepository,
            goldenHourRepository,
            rewardSelectionService
        );
    }

    @Test
    void concurrentSpinsShouldNotExceedRemainingSpins() throws InterruptedException {
        // Setup
        int numThreads = 10;
        Long eventId = 1L;
        Long participantId = 2L;
        int initialSpins = 5;

        Event event = Event.builder()
            .id(eventId)
            .isActive(true)
            .build();

        Participant participant = Participant.builder()
            .id(participantId)
            .remainingSpins(initialSpins)
            .build();

        Reward reward = Reward.builder()
            .id(3L)
            .name("Test Reward")
            .remainingQuantity(100)
            .build();

        // Mock repository responses
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(rewardRepository.findAvailableRewards(any(), any())).thenReturn(List.of(reward));
        when(goldenHourRepository.findActiveGoldenHour(any(), any())).thenReturn(Optional.empty());
        when(rewardSelectionService.selectReward(any(), any(), any(), any(), any())).thenReturn(reward);
        when(rewardRepository.decrementRemainingQuantityById(any())).thenReturn(1);
        
        // Mock participant save to simulate atomic decrement
        when(participantRepository.save(any())).thenAnswer(invocation -> {
            Participant p = invocation.getArgument(0);
            if (p.getRemainingSpins() < 0) {
                throw new IllegalStateException("Negative spins not allowed");
            }
            return p;
        });

        when(spinHistoryRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Create countdown latch for synchronization
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        // Track successful spins
        Set<Long> successfulSpins = ConcurrentHashMap.newKeySet();
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        // Create and start threads
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(() -> {
                try {
                    startLatch.await(); // Wait for start signal
                    
                    SpinRequest request = SpinRequest.builder()
                        .eventId(eventId)
                        .participantId(participantId)
                        .location("TEST")
                        .hasActiveParticipation(true)
                        .build();

                    try {
                        SpinResultDTO result = spinService.spinAndGetResult(request);
                        if (result.getWon()) {
                            successfulSpins.add(result.getRewardId());
                        }
                    } catch (Exception e) {
                        errors.add(e);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
            threads.add(t);
            t.start();
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to complete
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS), "Timeout waiting for threads");

        // Verify
        assertTrue(successfulSpins.size() <= initialSpins, 
            "Number of successful spins (" + successfulSpins.size() + ") should not exceed initial spins (" + initialSpins + ")");
        
        assertTrue(errors.stream().noneMatch(e -> e instanceof IllegalStateException),
            "No negative spin counts should occur");
    }
}
