package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.RewardRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Golden Hour Service Tests")
class GoldenHourServiceTest {

    @Mock
    private GoldenHourRepository goldenHourRepository;

    @Mock
    private RewardRepository rewardRepository;

    private GoldenHourMapper goldenHourMapper;
    private GoldenHourService goldenHourService;
    private LocalDateTime now;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        goldenHourMapper = new GoldenHourMapper();
        goldenHourService = new GoldenHourService(goldenHourRepository, rewardRepository, goldenHourMapper);
        now = LocalDateTime.now().withHour(12).withMinute(0).withSecond(0).withNano(0); // Noon for consistent testing
        testEvent = createValidEvent();
    }

    @Test
    @DisplayName("Should get active golden hour for event")
    void shouldGetActiveGoldenHour() {
        // Given
        GoldenHour goldenHour = createValidGoldenHour();
        LocalDateTime queryTime = goldenHour.getStartTime().plusMinutes(30);

        when(goldenHourRepository.findActiveGoldenHour(eq(testEvent.getId()), eq(queryTime)))
                .thenReturn(Optional.of(goldenHour));

        // When
        Optional<GoldenHour> result = goldenHourService.findActiveGoldenHour(testEvent.getId(), queryTime);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(goldenHour.getId());
        verify(goldenHourRepository).findActiveGoldenHour(eq(testEvent.getId()), eq(queryTime));
    }

    @Test
    @DisplayName("Should get golden hour multiplier during active period")
    void shouldGetGoldenHourMultiplierDuringActivePeriod() {
        // Given
        GoldenHour goldenHour = createValidGoldenHour();
        LocalDateTime queryTime = goldenHour.getStartTime().plusMinutes(30);

        when(goldenHourRepository.findById(1L)).thenReturn(Optional.of(goldenHour));
        // when(goldenHourRepository.findActiveGoldenHour(eq(testEvent.getId()), eq(queryTime)))
        //         .thenReturn(Optional.of(goldenHour));

        // When
        Double result = goldenHourService.getGoldenHourMultiplier(testEvent.getId(), 1L, queryTime);

        // Then
        assertThat(result).isEqualTo(goldenHour.getMultiplier());
    }

    @Test
    @DisplayName("Should get default multiplier when no golden hour exists")
    void shouldGetDefaultMultiplierWhenNoGoldenHourExists() {
        // When
        Double result = goldenHourService.getGoldenHourMultiplier(testEvent.getId(), null, now);

        // Then
        assertThat(result).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should get default multiplier outside golden hour period")
    void shouldGetDefaultMultiplierOutsideGoldenHourPeriod() {
        // Given
        GoldenHour goldenHour = createValidGoldenHour();
        LocalDateTime queryTime = goldenHour.getEndTime().plusMinutes(1);

        when(goldenHourRepository.findById(1L)).thenReturn(Optional.of(goldenHour));
        // when(goldenHourRepository.findActiveGoldenHour(eq(testEvent.getId()), eq(queryTime)))
        //         .thenReturn(Optional.empty());

        // When
        Double result = goldenHourService.getGoldenHourMultiplier(testEvent.getId(), 1L, queryTime);

        // Then
        assertThat(result).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should create golden hour successfully")
    void shouldCreateGoldenHour() {
        // Given
        GoldenHourDTO.CreateRequest request = GoldenHourDTO.CreateRequest.builder()
                .name("Test Golden Hour")
                .multiplier(2.0)
                .startTime(now)
                .endTime(now.plusHours(1))
                .isActive(true)
                .build();

        Reward reward = createValidReward();
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(reward));

        GoldenHour goldenHour = goldenHourMapper.createEntity(request);
        goldenHour.setId(1L);
        goldenHour.setReward(reward);

        when(goldenHourRepository.save(any(GoldenHour.class))).thenReturn(goldenHour);

        // When
        GoldenHourDTO result = goldenHourService.createGoldenHour(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getMultiplier()).isEqualTo(request.getMultiplier());
        verify(goldenHourRepository).save(any(GoldenHour.class));
    }

    private Event createValidEvent() {
        return Event.builder()
                .id(1L)
                .name("Test Event")
                .code("TEST_EVENT")
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(7))
                .isActive(true)
                .build();
    }

    private GoldenHour createValidGoldenHour() {
        return GoldenHour.builder()
                .id(1L)
                .name("Test Golden Hour")
                .multiplier(2.0)
                .startTime(now)
                .endTime(now.plusHours(1))
                .isActive(true)
                .reward(createValidReward())
                .build();
    }

    private Reward createValidReward() {
        Reward reward = Reward.builder()
                .id(1L)
                .name("Test Reward")
                .quantity(100)
                .remainingQuantity(50)
                .probability(0.5)
                .isActive(true)
                .build();
        reward.setEvent(testEvent);
        return reward;
    }
}