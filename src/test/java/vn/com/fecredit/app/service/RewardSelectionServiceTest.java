package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.RewardRepository;

@ExtendWith(MockitoExtension.class)
class RewardSelectionServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private RewardSelectionService rewardSelectionService;

    private Event event;
    private Reward reward;

    @BeforeEach
    void setUp() {
        event = Event.builder()
            .id(1L)
            .code("TEST-EVENT")
            .name("Test Event")
            .isActive(true)
            .build();

        reward = Reward.builder()
            .id(1L)
            .event(event)
            .name("Test Reward")
            .quantity(100)
            .remainingQuantity(100)
            .probability(1.0)
            .isActive(true)
            .build();
        reward.setApplicableProvincesFromString("HN,HCM");
    }

    @Test
    void selectReward_WithValidProvinceAndQuantity_ShouldReturnReward() {
        // Given
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(rewardRepository.findAvailableRewards(anyLong(), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(reward));
        when(rewardRepository.save(any(Reward.class))).thenReturn(reward);

        // When
        Optional<Reward> result = rewardSelectionService.selectReward(1L, "HN");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Reward");
        assertThat(result.get().getRemainingQuantity()).isEqualTo(99);
    }

    @Test
    void selectReward_WithInvalidProvince_ShouldReturnEmpty() {
        // Given
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(rewardRepository.findAvailableRewards(anyLong(), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(reward));

        // When
        Optional<Reward> result = rewardSelectionService.selectReward(1L, "DN");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void selectReward_WithNoRemainingQuantity_ShouldReturnEmpty() {
        // Given
        reward.setRemainingQuantity(0);
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(rewardRepository.findAvailableRewards(anyLong(), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(reward));

        // When
        Optional<Reward> result = rewardSelectionService.selectReward(1L, "HN");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void selectReward_WithInactiveEvent_ShouldReturnEmpty() {
        // Given
        event.setActive(false);
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));

        // When
        Optional<Reward> result = rewardSelectionService.selectReward(1L, "HN");

        // Then
        assertThat(result).isEmpty();
    }
}