package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.dto.golden.CreateGoldenHourRequest;
import vn.com.fecredit.app.dto.golden.UpdateGoldenHourRequest;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.RewardRepository;

class GoldenHourServiceTest {

    @Mock
    private GoldenHourRepository goldenHourRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private GoldenHourMapper goldenHourMapper;

    @InjectMocks
    private GoldenHourService goldenHourService;

    private Event testEvent;
    private Reward testReward;
    private GoldenHour testGoldenHour;
    private GoldenHourDTO testDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

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
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .multiplier(2.0)
                .isActive(true)
                .build();

        testDTO = GoldenHourDTO.builder()
                .id(1L)
                .eventId(testEvent.getId())
                .rewardId(testReward.getId())
                .name("Test Golden Hour")
                .multiplier(2.0)
                .isActive(true)
                .build();
    }

    @Test
    void getAllByEventId_Success() {
        when(goldenHourRepository.findByEventIdAndIsActiveTrue(1L))
                .thenReturn(Arrays.asList(testGoldenHour));
        when(goldenHourMapper.toDTO(testGoldenHour)).thenReturn(testDTO);

        List<GoldenHourDTO> result = goldenHourService.getAllByEventId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testDTO);
    }

    @Test
    void getById_Success() {
        when(goldenHourRepository.findById(1L)).thenReturn(Optional.of(testGoldenHour));
        when(goldenHourMapper.toDTO(testGoldenHour)).thenReturn(testDTO);

        GoldenHourDTO result = goldenHourService.getById(1L);

        assertThat(result).isEqualTo(testDTO);
    }

    @Test
    void getById_NotFound() {
        when(goldenHourRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            goldenHourService.getById(1L);
        });
    }

    @Test
    void create_Success() {
        CreateGoldenHourRequest request = CreateGoldenHourRequest.builder()
                .eventId(1L)
                .rewardId(1L)
                .name("Test Golden Hour")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .multiplier(2.0)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(testReward));
        when(goldenHourMapper.createEntityFromRequest(request)).thenReturn(testGoldenHour);
        when(goldenHourRepository.save(any(GoldenHour.class))).thenReturn(testGoldenHour);
        when(goldenHourMapper.toDTO(testGoldenHour)).thenReturn(testDTO);

        GoldenHourDTO result = goldenHourService.create(request);

        assertThat(result).isEqualTo(testDTO);
    }

    @Test
    void update_Success() {
        UpdateGoldenHourRequest request = UpdateGoldenHourRequest.builder()
                .name("Updated Golden Hour")
                .multiplier(3.0)
                .build();

        when(goldenHourRepository.findById(1L)).thenReturn(Optional.of(testGoldenHour));
        when(goldenHourRepository.save(any(GoldenHour.class))).thenReturn(testGoldenHour);
        when(goldenHourMapper.toDTO(testGoldenHour)).thenReturn(testDTO);

        GoldenHourDTO result = goldenHourService.update(1L, request);

        assertThat(result).isEqualTo(testDTO);
    }

    @Test
    void delete_Success() {
        when(goldenHourRepository.findById(1L)).thenReturn(Optional.of(testGoldenHour));
        when(goldenHourRepository.save(any(GoldenHour.class))).thenReturn(testGoldenHour);

        goldenHourService.delete(1L);

        assertThat(testGoldenHour.isActive()).isFalse();
    }

    @Test
    void getCurrentGoldenHour_Success() {
        when(goldenHourRepository.findCurrentGoldenHour(1L, LocalDateTime.now()))
                .thenReturn(Optional.of(testGoldenHour));
        when(goldenHourMapper.toDTO(testGoldenHour)).thenReturn(testDTO);

        Optional<GoldenHourDTO> result = goldenHourService.getCurrentGoldenHour(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testDTO);
    }
}