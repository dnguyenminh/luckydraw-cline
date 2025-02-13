package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.dto.SpinRequest;
import vn.com.fecredit.app.exception.BusinessException;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.LuckyDrawResult;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.model.SpinHistory;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.LuckyDrawResultRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;

@ExtendWith(MockitoExtension.class)
class SpinServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private ParticipantRepository participantRepository;
    @Mock private RewardRepository rewardRepository;
    @Mock private SpinHistoryRepository spinHistoryRepository;
    @Mock private LuckyDrawResultRepository luckyDrawResultRepository;
    @Mock private GoldenHourRepository goldenHourRepository;
    @Mock private RewardSelectionService rewardSelectionService;

    private SpinService spinService;
    private Event event;
    private Participant participant;
    private Reward reward;
    private SpinRequest request;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        spinService = new SpinService(
            eventRepository,
            participantRepository,
            rewardRepository,
            spinHistoryRepository,
            luckyDrawResultRepository,
            goldenHourRepository,
            rewardSelectionService
        );

        now = LocalDateTime.now();
        event = Event.builder()
                .id(1L)
                .code("TEST001")
                .name("Test Event")
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(1))
                .totalSpins(100L)
                .remainingSpins(50L)
                .isActive(true)
                .build();

        participant = Participant.builder()
                .id(1L)
                .event(event)
                .isActive(true)
                .build();

        reward = Reward.builder()
                .id(1L)
                .name("Test Reward")
                .quantity(10)
                .remainingQuantity(5)
                .isActive(true)
                .build();

        request = SpinRequest.builder()
                .eventId(1L)
                .participantId(1L)
                .customerLocation("Location1")
                .isGoldenHourEligible(true)
                .hasActiveParticipation(true)
                .remainingSpinsForParticipant(3L)
                .participantStatus("ACTIVE")
                .build();
    }

    @Test
    void shouldSuccessfullySpinAndWin() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(rewardRepository.findActiveRewardsByEventId(1L)).thenReturn(List.of(reward));
        when(goldenHourRepository.findActiveGoldenHour(eq(event.getId()), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(rewardSelectionService.selectReward(
                any(Event.class),
                anyList(),
                anyLong(),
                any(),
                anyString()))
            .thenReturn(Optional.of(reward));

        SpinHistory spinHistory = SpinHistory.builder()
            .event(event)
            .participant(participant)
            .reward(reward)
            .won(true)
            .result("WIN")
            .spinTime(now)
            .remainingSpins(49L)
            .build();

        when(spinHistoryRepository.save(any())).thenReturn(spinHistory);
        when(luckyDrawResultRepository.save(any())).thenReturn(mock(LuckyDrawResult.class));

        SpinHistory result = spinService.spin(request);

        assertThat(result).isNotNull();
        assertThat(result.getWon()).isTrue();
        assertThat(result.getReward()).isEqualTo(reward);
        verify(spinHistoryRepository).save(any());
        verify(luckyDrawResultRepository).save(any());
    }

    @Test
    void shouldThrowExceptionForInactiveEvent() {
        event.setIsActive(false);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> spinService.checkSpinEligibility(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Event is not active");
    }

    @Test
    void shouldThrowExceptionWhenNoRemainingSpins() {
        event.setRemainingSpins(0L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> spinService.checkSpinEligibility(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("No spins available for this event");
    }

    @Test
    void shouldReturnNullForNonExistentSpinHistory() {
        when(spinHistoryRepository.findFirstByParticipantIdOrderBySpinTimeDesc(1L))
            .thenReturn(Optional.empty());

        SpinHistory result = spinService.getLatestSpinHistory(1L);
        assertThat(result).isNull();
    }

    @Test
    void shouldCalculateCorrectGoldenHourMultiplier() {
        GoldenHour goldenHour = GoldenHour.builder()
            .multiplier(2.0)
            .isActive(true)
            .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(rewardRepository.findActiveRewardsByEventId(1L)).thenReturn(List.of(reward));
        when(goldenHourRepository.findActiveGoldenHour(eq(event.getId()), any(LocalDateTime.class))).thenReturn(Optional.of(goldenHour));
        when(rewardSelectionService.selectReward(any(Event.class), anyList(), anyLong(), any(), anyString()))
            .thenReturn(Optional.of(reward));

        SpinHistory spinHistory = SpinHistory.builder()
            .event(event)
            .participant(participant)
            .reward(reward)
            .won(true)
            .result("WIN")
            .spinTime(now)
            .isGoldenHour(true)
            .currentMultiplier(2.0)
            .remainingSpins(49L)
            .build();

        when(spinHistoryRepository.save(any())).thenReturn(spinHistory);

        SpinHistory result = spinService.spin(request);

        assertThat(result.getIsGoldenHour()).isTrue();
        assertThat(result.getCurrentMultiplier()).isEqualTo(2.0);
    }
}