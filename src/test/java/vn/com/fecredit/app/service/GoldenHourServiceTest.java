package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.RewardRepository;

@ExtendWith(MockitoExtension.class)
class GoldenHourServiceTest {

    @Mock
    private GoldenHourRepository goldenHourRepository;

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private GoldenHourMapper goldenHourMapper;

    @InjectMocks
    private GoldenHourService goldenHourService;

    private Reward reward;
    private GoldenHour goldenHour;
    private GoldenHourDTO.CreateRequest createRequest;
    private GoldenHourDTO goldenHourDTO;

    @BeforeEach
    void setUp() {
        reward = Reward.builder()
            .id(1L)
            .name("Test Reward")
            .build();

        goldenHour = GoldenHour.builder()
            .id(1L)
            .reward(reward)
            .name("Test Golden Hour")
            .startHour(9)
            .endHour(17)
            .multiplier(2.0)
            .isActive(true)
            .build();

        createRequest = GoldenHourDTO.CreateRequest.builder()
            .name("Test Golden Hour")
            .startHour(9)
            .endHour(17)
            .multiplier(2.0)
            .isActive(true)
            .build();

        goldenHourDTO = GoldenHourDTO.builder()
            .id(1L)
            .rewardId(1L)
            .name("Test Golden Hour")
            .startHour(9)
            .endHour(17)
            .multiplier(2.0)
            .isActive(true)
            .build();
    }

    @Test
    void createGoldenHour_WithValidRequest_ShouldReturnCreatedDTO() {
        // Given
        when(rewardRepository.findById(eq(1L))).thenReturn(Optional.of(reward));
        when(goldenHourMapper.toEntity(eq(createRequest))).thenReturn(goldenHour);
        when(goldenHourRepository.save(any(GoldenHour.class))).thenReturn(goldenHour);
        when(goldenHourMapper.toDTO(any(GoldenHour.class))).thenReturn(goldenHourDTO);

        // When
        GoldenHourDTO result = goldenHourService.createGoldenHour(1L, createRequest);

        // Then
        assertThat(result).isEqualTo(goldenHourDTO);
        verify(rewardRepository).findById(eq(1L));
        verify(goldenHourRepository).save(any(GoldenHour.class));
    }

    @Test
    void createGoldenHour_WithNonExistentReward_ShouldThrowException() {
        // Given
        when(rewardRepository.findById(eq(1L))).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> goldenHourService.createGoldenHour(1L, createRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Reward not found");
    }

    @Test
    void getGoldenHourMultiplier_WithActiveGoldenHour_ShouldReturnMultiplier() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        when(goldenHourRepository.findActiveGoldenHour(eq(1L), any(LocalDateTime.class)))
            .thenReturn(Optional.of(goldenHour));

        // When
        Double multiplier = goldenHourService.getGoldenHourMultiplier(1L, null, now);

        // Then
        assertThat(multiplier).isEqualTo(2.0);
    }

    @Test
    void getGoldenHourMultiplier_WithNoActiveGoldenHour_ShouldReturnDefaultMultiplier() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        when(goldenHourRepository.findActiveGoldenHour(eq(1L), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        // When
        Double multiplier = goldenHourService.getGoldenHourMultiplier(1L, null, now);

        // Then
        assertThat(multiplier).isEqualTo(1.0);
    }

    @Test
    void findActiveByRewardIdWithDetails_ShouldReturnActiveGoldenHours() {
        // Given
        List<GoldenHour> goldenHours = List.of(goldenHour);
        when(goldenHourRepository.findActiveByRewardIdWithDetails(eq(1L)))
            .thenReturn(goldenHours);

        // When
        List<GoldenHour> result = goldenHourService.findActiveByRewardIdWithDetails(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(goldenHour);
    }
}