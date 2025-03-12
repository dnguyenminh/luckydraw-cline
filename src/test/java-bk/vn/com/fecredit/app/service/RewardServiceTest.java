package vn.com.fecredit.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.OptimisticLockException;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.RewardRepository;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        rewardService = new RewardService(rewardRepository);
    }

    @Test
    void shouldFindActiveRewards() {
        // Setup
        Event event = Event.builder()
                .id(1L)
                .isActive(true)
                .build();

        Reward reward = Reward.builder()
                .id(1L)
                .name("Test Reward")
                .isActive(true)
                .remainingQuantity(10)
                .event(event)
                .build();

        when(rewardRepository.findAvailableRewards(eq(1L), any()))
                .thenReturn(Arrays.asList(reward));

        // Execute
        List<Reward> rewards = rewardService.findAvailableRewards(1L);

        // Verify
        assertFalse(rewards.isEmpty());
        assertEquals(1, rewards.size());
        assertEquals("Test Reward", rewards.get(0).getName());
    }

    @Test
    void shouldDecrementRemainingQuantity() {
        // Setup
        Long rewardId = 1L;
        when(rewardRepository.decrementRemainingQuantityById(rewardId)).thenReturn(1);

        // Execute
        boolean result = rewardService.decrementRemainingQuantity(rewardId);

        // Verify
        assertTrue(result);
        verify(rewardRepository).decrementRemainingQuantityById(rewardId);
    }

    @Test
    void shouldHandleOptimisticLockingFailure() {
        // Setup
        Long rewardId = 1L;
        when(rewardRepository.decrementRemainingQuantityById(rewardId))
                .thenThrow(OptimisticLockException.class);

        // Execute & Verify
        assertFalse(rewardService.decrementRemainingQuantity(rewardId));
    }

    @Test
    void shouldFindByIdWithValidation() {
        // Setup
        Long rewardId = 1L;
        Reward reward = Reward.builder()
                .id(rewardId)
                .name("Test Reward")
                .build();

        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(reward));

        // Execute
        Reward found = rewardService.findByIdOrThrow(rewardId);

        // Verify
        assertNotNull(found);
        assertEquals(rewardId, found.getId());
        assertEquals("Test Reward", found.getName());
    }

    @Test
    void shouldThrowExceptionWhenRewardNotFound() {
        // Setup
        Long rewardId = 1L;
        when(rewardRepository.findById(rewardId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(ResourceNotFoundException.class, () -> rewardService.findByIdOrThrow(rewardId));
    }

    @Test
    void shouldFindAllWithPagination() {
        // Setup
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Reward> rewards = Arrays.asList(
            Reward.builder().id(1L).name("Reward 1").build(),
            Reward.builder().id(2L).name("Reward 2").build()
        );
        Page<Reward> page = new PageImpl<>(rewards, pageRequest, rewards.size());

        when(rewardRepository.findAll(pageRequest)).thenReturn(page);

        // Execute
        Page<Reward> result = rewardService.findAll(pageRequest);

        // Verify
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldUpdateRewardQuantity() {
        // Setup
        Long rewardId = 1L;
        Reward reward = Reward.builder()
                .id(rewardId)
                .remainingQuantity(10)
                .build();

        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(reward));
        when(rewardRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Execute
        Reward updated = rewardService.updateQuantity(rewardId, 20);

        // Verify
        assertNotNull(updated);
        assertEquals(20, updated.getRemainingQuantity());

        ArgumentCaptor<Reward> rewardCaptor = ArgumentCaptor.forClass(Reward.class);
        verify(rewardRepository).save(rewardCaptor.capture());
        assertEquals(20, rewardCaptor.getValue().getRemainingQuantity());
    }

    @Test
    void shouldNotUpdateInvalidQuantity() {
        // Setup
        Long rewardId = 1L;
        Reward reward = Reward.builder()
                .id(rewardId)
                .remainingQuantity(10)
                .build();

        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(reward));

        // Execute & Verify
        assertThrows(IllegalArgumentException.class, 
            () -> rewardService.updateQuantity(rewardId, -1));

        verify(rewardRepository, never()).save(any());
    }

    @Test
    void shouldHandleConcurrentQuantityUpdates() {
        // Setup
        Long rewardId = 1L;
        Reward reward = Reward.builder()
                .id(rewardId)
                .remainingQuantity(10)
                .version(1L)
                .build();

        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(reward));
        when(rewardRepository.save(any())).thenThrow(OptimisticLockException.class);

        // Execute & Verify
        assertThrows(OptimisticLockException.class, 
            () -> rewardService.updateQuantity(rewardId, 20));
    }
}