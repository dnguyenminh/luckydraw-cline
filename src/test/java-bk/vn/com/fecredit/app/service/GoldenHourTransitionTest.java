package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.GoldenHourRepository;

class GoldenHourTransitionTest {

    @Mock
    private GoldenHourRepository goldenHourRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private GoldenHourMapper goldenHourMapper;

    @InjectMocks
    private GoldenHourService goldenHourService;

    private Event testEvent;
    private Reward testReward;
    private GoldenHour testGoldenHour;
    private GoldenHourDTO testDTO;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        now = LocalDateTime.now();

        testEvent = Event.builder()
                .id(1L)
                .name("Test Event")
                .build();

        testReward = Reward.builder()
                .id(1L)
                .name("Test Reward")
                .build();

        testGoldenHour = GoldenHour.builder()
                .id(1L)
                .event(testEvent)
                .reward(testReward)
                .name("Test Golden Hour")
                .startTime(now)
                .endTime(now.plusHours(2))
                .multiplier(2.0)
                .isActive(true)
                .build();

        testDTO = GoldenHourDTO.builder()
                .id(1L)
                .eventId(testEvent.getId())
                .rewardId(testReward.getId())
                .name("Test Golden Hour")
                .startTime(now)
                .endTime(now.plusHours(2))
                .multiplier(2.0)
                .isActive(true)
                .build();
    }

    @Test
    void getCurrentGoldenHour_BeforeStart_ReturnsEmpty() {
        LocalDateTime beforeStart = now.minusMinutes(5);
        when(goldenHourRepository.findCurrentGoldenHour(testEvent.getId(), beforeStart))
                .thenReturn(Optional.empty());

        Optional<GoldenHourDTO> result = goldenHourService.getCurrentGoldenHour(testEvent.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentGoldenHour_DuringPeriod_ReturnsGoldenHour() {
        LocalDateTime duringPeriod = now.plusHours(1);
        when(goldenHourRepository.findCurrentGoldenHour(testEvent.getId(), duringPeriod))
                .thenReturn(Optional.of(testGoldenHour));
        when(goldenHourMapper.toDTO(any(GoldenHour.class)))
                .thenReturn(testDTO);

        Optional<GoldenHourDTO> result = goldenHourService.getCurrentGoldenHour(testEvent.getId());
        
        assertThat(result).isPresent();
        assertThat(result.get().getMultiplier()).isEqualTo(2.0);
    }

    @Test
    void getCurrentGoldenHour_AfterEnd_ReturnsEmpty() {
        LocalDateTime afterEnd = now.plusHours(3);
        when(goldenHourRepository.findCurrentGoldenHour(testEvent.getId(), afterEnd))
                .thenReturn(Optional.empty());

        Optional<GoldenHourDTO> result = goldenHourService.getCurrentGoldenHour(testEvent.getId());
        assertThat(result).isEmpty();
    }
}