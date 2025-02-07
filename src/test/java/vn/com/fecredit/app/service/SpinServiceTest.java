package vn.com.fecredit.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vn.com.fecredit.app.dto.spin.SpinCheckResponse;
import vn.com.fecredit.app.dto.spin.SpinRequest;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.model.*;
import vn.com.fecredit.app.repository.LuckyDrawResultRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SpinServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private SpinHistoryRepository spinHistoryRepository;

    @Mock
    private LuckyDrawResultRepository luckyDrawResultRepository;

    private SpinService spinService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        spinService = new SpinService(
                rewardRepository,
                participantRepository,
                spinHistoryRepository,
                luckyDrawResultRepository
        );
    }

    @Test
    void checkSpinEligibility_ValidParticipant_ReturnsEligible() {
        // Arrange
        Long participantId = 1L;
        String eventCode = "TEST_EVENT";
        SpinRequest request = new SpinRequest(participantId, eventCode);

        Participant participant = new Participant();
        participant.setId(participantId);
        participant.setSpinsRemaining(5);

        Event event = new Event();
        event.setCode(eventCode);
        event.setIsActive(true);
        event.setStartDate(LocalDateTime.now().minusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(1));
        
        participant.setEvent(event);

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));

        // Act
        SpinCheckResponse response = spinService.checkSpinEligibility(request);

        // Assert
        assertTrue(response.isEligible());
        assertEquals("Eligible to spin", response.getMessage());
    }

    @Test
    void spin_ValidParticipant_ReturnsReward() {
        // Arrange
        Long participantId = 1L;
        String eventCode = "TEST_EVENT";
        SpinRequest request = new SpinRequest(participantId, eventCode);

        Participant participant = new Participant();
        participant.setId(participantId);
        participant.setSpinsRemaining(5);
        participant.setProvince("TestProvince");

        Event event = new Event();
        event.setCode(eventCode);
        event.setIsActive(true);
        event.setStartDate(LocalDateTime.now().minusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(1));

        Reward reward = Reward.builder()
                .id(1L)
                .name("Test Reward")
                .probability(0.5)
                .quantity(10)
                .remainingQuantity(5)
                .isActive(true)
                .event(event)
                .build();

        participant.setEvent(event);
        event.setRewards(Arrays.asList(reward));

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(rewardRepository.save(any(Reward.class))).thenReturn(reward);
        when(spinHistoryRepository.save(any(SpinHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        spinService.spin(request);

        // Verify
        verify(participantRepository).save(participant);
        verify(spinHistoryRepository).save(any(SpinHistory.class));
    }

    @Test
    void spin_ParticipantNotFound_ThrowsException() {
        // Arrange
        Long participantId = 1L;
        String eventCode = "TEST_EVENT";
        SpinRequest request = new SpinRequest(participantId, eventCode);

        when(participantRepository.findById(participantId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> spinService.spin(request));
    }

    @Test
    void spin_NoSpinsRemaining_ThrowsException() {
        // Arrange
        Long participantId = 1L;
        String eventCode = "TEST_EVENT";
        SpinRequest request = new SpinRequest(participantId, eventCode);

        Participant participant = new Participant();
        participant.setId(participantId);
        participant.setSpinsRemaining(0);

        Event event = new Event();
        event.setCode(eventCode);
        participant.setEvent(event);

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> spinService.spin(request));
    }
}