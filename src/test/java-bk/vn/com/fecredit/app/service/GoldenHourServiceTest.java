package vn.com.fecredit.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.enums.RecurringDay;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.service.impl.GoldenHourServiceImpl;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoldenHourServiceTest {

    @Mock
    private GoldenHourRepository goldenHourRepository;

    @Mock 
    private GoldenHourMapper mapper;

    @Mock
    private EventService eventService;

    @Mock
    private RewardService rewardService;

    @InjectMocks
    private GoldenHourServiceImpl goldenHourService;

    private Event testEvent;
    private Reward testReward;
    private GoldenHour testGoldenHour;
    private GoldenHourDTO.CreateRequest createRequest;
    private GoldenHourDTO.UpdateRequest updateRequest;
    private GoldenHourDTO.Response responseDTO;

    @BeforeEach
    void setUp() {
        testEvent = Event.builder()
            .id(1L)
            .name("Test Event")
            .status(EntityStatus.ACTIVE)
            .build();

        testReward = Reward.builder()
            .id(2L)
            .name("Test Reward")
            .status(EntityStatus.ACTIVE)
            .build();

        Set<RecurringDay> activeDays = new HashSet<>();
        activeDays.add(RecurringDay.MONDAY);
        activeDays.add(RecurringDay.WEDNESDAY);

        testGoldenHour = GoldenHour.builder()
            .id(3L)
            .name("Test Golden Hour")
            .description("Test Description")
            .startTime(LocalTime.of(10, 0).atDate(java.time.LocalDate.now()))
            .endTime(LocalTime.of(12, 0).atDate(java.time.LocalDate.now()))
            .multiplier(2.0)
            .probabilityMultiplier(1.5)
            .activeDays("13")  // Monday and Wednesday
            .event(testEvent)
            .reward(testReward)
            .status(EntityStatus.ACTIVE)
            .build();

        createRequest = GoldenHourDTO.CreateRequest.builder()
            .name("New Golden Hour")
            .description("New Description")
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(12, 0))
            .multiplier(2.0)
            .probabilityMultiplier(1.5)
            .activeDays(activeDays)
            .eventId(1L)
            .rewardId(2L)
            .build();

        updateRequest = GoldenHourDTO.UpdateRequest.builder()
            .name("Updated Golden Hour")
            .description("Updated Description")
            .startTime(LocalTime.of(14, 0))
            .endTime(LocalTime.of(16, 0))
            .multiplier(3.0)
            .probabilityMultiplier(2.0)
            .activeDays(activeDays)
            .rewardId(2L)
            .active(true)
            .build();

        responseDTO = GoldenHourDTO.Response.builder()
            .id(3L)
            .name("Test Golden Hour")
            .description("Test Description")
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(12, 0))
            .multiplier(2.0)
            .probabilityMultiplier(1.5)
            .activeDays(activeDays)
            .eventId(1L)
            .rewardId(2L)
            .eventName("Test Event")
            .rewardName("Test Reward")
            .active(true)
            .build();
    }

    @Test
    void create_shouldCreateNewGoldenHour() {
        // Given
        when(eventService.getById(1L)).thenReturn(testEvent);
        when(rewardService.getById(2L)).thenReturn(testReward);
        when(mapper.toEntity(createRequest)).thenReturn(testGoldenHour);
        when(goldenHourRepository.save(any(GoldenHour.class))).thenReturn(testGoldenHour);
        when(mapper.toResponse(testGoldenHour)).thenReturn(responseDTO);

        // When
        GoldenHourDTO.Response result = goldenHourService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(createRequest.getName());
        verify(goldenHourRepository).save(any(GoldenHour.class));
    }

    @Test
    void getById_shouldReturnGoldenHour() {
        // Given
        when(goldenHourRepository.findById(3L)).thenReturn(Optional.of(testGoldenHour));
        when(mapper.toResponse(testGoldenHour)).thenReturn(responseDTO);

        // When
        GoldenHourDTO.Response result = goldenHourService.getById(3L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testGoldenHour.getId());
    }

    @Test
    void update_shouldUpdateGoldenHour() {
        // Given
        when(goldenHourRepository.findById(3L)).thenReturn(Optional.of(testGoldenHour));
        when(goldenHourRepository.save(any(GoldenHour.class))).thenReturn(testGoldenHour);
        when(mapper.toResponse(testGoldenHour)).thenReturn(responseDTO);

        // When
        GoldenHourDTO.Response result = goldenHourService.update(3L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(goldenHourRepository).save(any(GoldenHour.class));
    }
}
