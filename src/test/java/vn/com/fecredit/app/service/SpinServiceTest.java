package vn.com.fecredit.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.dto.SpinRequest;
import vn.com.fecredit.app.dto.SpinResultDTO;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.exception.SpinNotAllowedException;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.model.SpinHistory;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;

@ExtendWith(MockitoExtension.class)
class SpinServiceTest {

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
    void spinWithValidRequestShouldSucceed() {
        // Setup
        Long eventId = 1L;
        Long participantId = 2L;
        Event event = Event.builder()
                .id(eventId)
                .isActive(true)
                .build();
        Participant participant = Participant.builder()
                .id(participantId)
                .remainingSpins(5)
                .build();
        Reward reward = Reward.builder()
                .id(3L)
                .name("Test Reward")
                .build();
        List<Reward> rewards = List.of(reward);

        SpinRequest request = SpinRequest.builder()
                .eventId(eventId)
                .participantId(participantId)
                .location("TEST")
                .hasActiveParticipation(true)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(rewardRepository.findAvailableRewards(any(), any())).thenReturn(rewards);
        when(goldenHourRepository.findActiveGoldenHour(any(), any())).thenReturn(Optional.empty());
        when(rewardSelectionService.selectReward(any(), any(), any(), any(), any())).thenReturn(reward);
        when(rewardRepository.decrementRemainingQuantityById(any())).thenReturn(1);
        when(spinHistoryRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Execute
        SpinResultDTO result = spinService.spinAndGetResult(request);

        // Verify
        assertNotNull(result);
        assertTrue(result.getWon());
        assertEquals(4L, result.getRemainingSpins());
        assertEquals(reward.getId(), result.getRewardId());
        assertEquals(reward.getName(), result.getRewardName());
    }

    @Test
    void spinWithNoRewardsShouldReturnUnsuccessfulSpin() {
        // Setup
        Long eventId = 1L;
        Long participantId = 2L;
        Event event = Event.builder()
                .id(eventId)
                .isActive(true)
                .build();
        Participant participant = Participant.builder()
                .id(participantId)
                .remainingSpins(5)
                .build();

        SpinRequest request = SpinRequest.builder()
                .eventId(eventId)
                .participantId(participantId)
                .location("TEST")
                .hasActiveParticipation(true)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(rewardRepository.findAvailableRewards(any(), any())).thenReturn(List.of());
        when(goldenHourRepository.findActiveGoldenHour(any(), any())).thenReturn(Optional.empty());
        when(rewardSelectionService.selectReward(any(), any(), any(), any(), any())).thenReturn(null);
        when(spinHistoryRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Execute
        SpinResultDTO result = spinService.spinAndGetResult(request);

        // Verify
        assertNotNull(result);
        assertFalse(result.getWon());
        assertEquals(4L, result.getRemainingSpins());
        assertNull(result.getRewardId());
        assertNull(result.getRewardName());
    }

    @Test
    void spinWithInactiveEventShouldThrowException() {
        // Setup
        Long eventId = 1L;
        Long participantId = 2L;
        Event event = Event.builder()
                .id(eventId)
                .isActive(false)
                .build();

        SpinRequest request = SpinRequest.builder()
                .eventId(eventId)
                .participantId(participantId)
                .location("TEST")
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Execute & Verify
        assertThrows(SpinNotAllowedException.class, () -> spinService.spin(request));
    }

    @Test
    void spinWithNoRemainingSpinsShouldThrowException() {
        // Setup
        Long eventId = 1L;
        Long participantId = 2L;
        Event event = Event.builder()
                .id(eventId)
                .isActive(true)
                .build();
        Participant participant = Participant.builder()
                .id(participantId)
                .remainingSpins(0)
                .build();

        SpinRequest request = SpinRequest.builder()
                .eventId(eventId)
                .participantId(participantId)
                .location("TEST")
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));

        // Execute & Verify
        assertThrows(SpinNotAllowedException.class, () -> spinService.spin(request));
    }

    @Test
    void spinWithInvalidEventShouldThrowException() {
        // Setup
        Long eventId = 1L;
        Long participantId = 2L;

        SpinRequest request = SpinRequest.builder()
                .eventId(eventId)
                .participantId(participantId)
                .location("TEST")
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(ResourceNotFoundException.class, () -> spinService.spin(request));
    }

    @Test
    void spinDuringGoldenHourShouldApplyMultiplier() {
        // Setup
        Long eventId = 1L;
        Long participantId = 2L;
        Event event = Event.builder()
                .id(eventId)
                .isActive(true)
                .build();
        Participant participant = Participant.builder()
                .id(participantId)
                .remainingSpins(5)
                .build();
        Reward reward = Reward.builder()
                .id(3L)
                .name("Test Reward")
                .build();
        GoldenHour goldenHour = GoldenHour.builder()
                .multiplier(2.0)
                .build();

        SpinRequest request = SpinRequest.builder()
                .eventId(eventId)
                .participantId(participantId)
                .location("TEST")
                .isGoldenHourEligible(true)
                .hasActiveParticipation(true)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(rewardRepository.findAvailableRewards(any(), any())).thenReturn(List.of(reward));
        when(goldenHourRepository.findActiveGoldenHour(any(), any())).thenReturn(Optional.of(goldenHour));
        when(rewardSelectionService.selectReward(any(), any(), any(), any(), any())).thenReturn(reward);
        when(rewardRepository.decrementRemainingQuantityById(any())).thenReturn(1);
        when(spinHistoryRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Execute
        SpinResultDTO result = spinService.spinAndGetResult(request);

        // Verify
        assertNotNull(result);
        assertTrue(result.getWon());
        assertTrue(result.getIsGoldenHour());
        assertEquals(2.0, result.getMultiplier());
    }
}