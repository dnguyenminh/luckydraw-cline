package vn.com.fecredit.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.RewardRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Golden Hour Transition Tests")
class GoldenHourTransitionTest {

    @Mock
    private GoldenHourRepository goldenHourRepository;

    @Mock
    private RewardRepository rewardRepository;

    private GoldenHourMapper goldenHourMapper;
    private GoldenHourService goldenHourService;
    private Event testEvent;
    private Reward testReward;
    private LocalDateTime baseDateTime;

    @BeforeEach
    void setUp() {
        goldenHourMapper = new GoldenHourMapper();
        goldenHourService = new GoldenHourService(goldenHourRepository, rewardRepository, goldenHourMapper);
        baseDateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        testEvent = createTestEvent();
        testReward = createTestReward();
    }

    @Test
    @DisplayName("Should handle exact boundary transitions")
    void shouldHandleExactBoundaryTransitions() {
        // Given
        LocalDateTime startTime = baseDateTime.withHour(14).withMinute(0);    // 2:00 PM
        LocalDateTime endTime = baseDateTime.withHour(15).withMinute(0);      // 3:00 PM

        // Test time points
        LocalDateTime beforeStart = startTime.minusSeconds(1);                 // 1:59:59 PM
        LocalDateTime atStart = startTime;                                     // 2:00:00 PM
        LocalDateTime duringPeriod = startTime.plusMinutes(30);               // 2:30:00 PM
        LocalDateTime atEnd = endTime;                                         // 3:00:00 PM
        LocalDateTime afterEnd = endTime.plusSeconds(1);                      // 3:00:01 PM

        GoldenHour goldenHour = GoldenHour.builder()
                .id(1L)
                .name("Afternoon Golden Hour")
                .multiplier(2.5)
                .startTime(startTime)
                .endTime(endTime)
                .isActive(true)
                .reward(testReward)
                .build();

        // when(goldenHourRepository.findById(1L)).thenReturn(Optional.of(goldenHour));
        when(goldenHourRepository.findActiveGoldenHourByRewardId(eq(testEvent.getId()), eq(beforeStart))).thenReturn(Optional.empty());
        when(goldenHourRepository.findActiveGoldenHourByRewardId(eq(testEvent.getId()), eq(atStart))).thenReturn(Optional.of(goldenHour));
        when(goldenHourRepository.findActiveGoldenHourByRewardId(eq(testEvent.getId()), eq(duringPeriod))).thenReturn(Optional.of(goldenHour));
        when(goldenHourRepository.findActiveGoldenHourByRewardId(eq(testEvent.getId()), eq(atEnd))).thenReturn(Optional.empty());
        when(goldenHourRepository.findActiveGoldenHourByRewardId(eq(testEvent.getId()), eq(afterEnd))).thenReturn(Optional.empty());

        // When & Then
        // Test with no specific golden hour (null goldenHourId)
        assertThat(goldenHourService.getGoldenHourMultiplier(testEvent.getId(), null, beforeStart)).isEqualTo(1.0);  // Before start -> default
        assertThat(goldenHourService.getGoldenHourMultiplier(testEvent.getId(), null, atStart)).isEqualTo(2.5);      // At start -> active
        assertThat(goldenHourService.getGoldenHourMultiplier(testEvent.getId(), null, duringPeriod)).isEqualTo(2.5); // During -> active
        assertThat(goldenHourService.getGoldenHourMultiplier(testEvent.getId(), null, atEnd)).isEqualTo(1.0);        // At end -> default
        assertThat(goldenHourService.getGoldenHourMultiplier(testEvent.getId(), null, afterEnd)).isEqualTo(1.0);     // After end -> default
    }

    @Test
    @DisplayName("Should handle inactive golden hours")
    void shouldHandleInactiveGoldenHours() {
        // Given
        LocalDateTime activeTime = baseDateTime.withHour(14).withMinute(30);
        GoldenHour inactiveGoldenHour = GoldenHour.builder()
                .id(1L)
                .name("Inactive Golden Hour")
                .multiplier(2.5)
                .startTime(baseDateTime.withHour(14).withMinute(0))
                .endTime(baseDateTime.withHour(15).withMinute(0))
                .isActive(false)
                .reward(testReward)
                .build();

//        when(goldenHourRepository.findById(1L)).thenReturn(Optional.of(inactiveGoldenHour));
//        when(goldenHourRepository.findActiveGoldenHour(eq(testEvent.getId()), any(LocalDateTime.class))).thenReturn(Optional.empty());

        // When & Then
        assertThat(goldenHourService.getGoldenHourMultiplier(testEvent.getId(), 1L, activeTime)).isEqualTo(1.0);
        assertThat(goldenHourService.getGoldenHourMultiplier(testEvent.getId(), null, activeTime)).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should handle overlapping golden hours by ID")
    void shouldHandleOverlappingGoldenHoursByID() {
        // Given
        LocalDateTime overlapTime = baseDateTime.withHour(14).withMinute(30);
        GoldenHour firstGoldenHour = GoldenHour.builder()
                .id(1L)
                .name("First Golden Hour")
                .multiplier(2.0)
                .startTime(baseDateTime.withHour(14).withMinute(0))
                .endTime(baseDateTime.withHour(15).withMinute(0))
                .isActive(true)
                .reward(testReward)
                .build();

        GoldenHour secondGoldenHour = GoldenHour.builder()
                .id(2L)
                .name("Second Golden Hour")
                .multiplier(3.0)
                .startTime(baseDateTime.withHour(14).withMinute(30))
                .endTime(baseDateTime.withHour(15).withMinute(30))
                .isActive(true)
                .reward(testReward)
                .build();

        when(goldenHourRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(firstGoldenHour));
        when(goldenHourRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(secondGoldenHour));
        when(goldenHourRepository.findActiveGoldenHourByRewardId(eq(testEvent.getId()), eq(overlapTime))).thenReturn(Optional.of(firstGoldenHour));

        // When & Then
        assertThat(goldenHourService.getGoldenHourMultiplier(testEvent.getId(), 1L, overlapTime)).isEqualTo(2.0);
        assertThat(goldenHourService.getGoldenHourMultiplier(testEvent.getId(), 2L, overlapTime)).isEqualTo(3.0);
        assertThat(goldenHourService.getGoldenHourMultiplier(testEvent.getId(), null, overlapTime)).isEqualTo(2.0);
    }

    private Event createTestEvent() {
        return Event.builder()
                .id(1L)
                .name("Test Event")
                .code("TEST_EVENT")
                .startDate(baseDateTime.minusDays(1))
                .endDate(baseDateTime.plusDays(7))
                .isActive(true)
                .build();
    }

    private Reward createTestReward() {
        return Reward.builder()
                .id(1L)
                .name("Test Reward")
                .quantity(100)
                .remainingQuantity(50)
                .probability(0.5)
                .isActive(true)
                .event(testEvent)
                .build();
    }
}