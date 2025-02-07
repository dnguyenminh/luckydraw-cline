package vn.com.fecredit.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.RewardRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoldenHourServiceTest {

    @Mock
    private GoldenHourRepository goldenHourRepository;
    
    @Mock
    private RewardRepository rewardRepository;
    
    @Mock
    private GoldenHourMapper goldenHourMapper;

    private GoldenHourService goldenHourService;

    @BeforeEach
    void setUp() {
        goldenHourService = new GoldenHourService(
            goldenHourRepository,
            rewardRepository,
            goldenHourMapper
        );
    }

    @Test
    void createGoldenHour_ShouldCreateAndReturnGoldenHour() {
        // Given
        Long rewardId = 1L;
        LocalDateTime now = LocalDateTime.now();
        
        GoldenHourDTO.CreateRequest createRequest = GoldenHourDTO.CreateRequest.builder()
                .name("Test Golden Hour")
                .multiplier(2.0)
                .startTime(now.minusHours(1))
                .endTime(now.plusHours(1))
                .isActive(true)
                .build();

        Reward reward = Reward.builder()
                .id(rewardId)
                .name("Test Reward")
                .build();

        GoldenHour goldenHour = GoldenHour.builder()
                .name(createRequest.getName())
                .multiplier(createRequest.getMultiplier())
                .startTime(createRequest.getStartTime().toLocalTime())
                .endTime(createRequest.getEndTime().toLocalTime())
                .isActive(createRequest.getIsActive())
                .reward(reward)
                .build();

        GoldenHourDTO expectedDTO = GoldenHourDTO.builder()
                .id(1L)
                .name("Test Golden Hour")
                .multiplier(2.0)
                .startTime(now.minusHours(1))
                .endTime(now.plusHours(1))
                .isActive(true)
                .rewardId(rewardId)
                .build();

        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(reward));
        when(goldenHourMapper.createEntity(createRequest)).thenReturn(goldenHour);
        when(goldenHourRepository.save(any(GoldenHour.class))).thenReturn(goldenHour);
        when(goldenHourMapper.toDTO(goldenHour)).thenReturn(expectedDTO);

        // When
        GoldenHourDTO result = goldenHourService.createGoldenHour(rewardId, createRequest);

        // Then
        assertThat(result).isEqualTo(expectedDTO);
        verify(goldenHourRepository).save(any(GoldenHour.class));
    }

    @Test
    void updateGoldenHour_ShouldUpdateAndReturnGoldenHour() {
        // Given
        Long goldenHourId = 1L;
        LocalDateTime now = LocalDateTime.now();
        
        GoldenHourDTO.UpdateRequest updateRequest = GoldenHourDTO.UpdateRequest.builder()
                .name("Updated Golden Hour")
                .multiplier(3.0)
                .startTime(now.minusHours(2))
                .endTime(now.plusHours(2))
                .isActive(true)
                .build();

        GoldenHour existingGoldenHour = GoldenHour.builder()
                .id(goldenHourId)
                .name("Test Golden Hour")
                .multiplier(2.0)
                .startTime(LocalTime.now().minusHours(1))
                .endTime(LocalTime.now().plusHours(1))
                .isActive(true)
                .build();

        GoldenHour updatedGoldenHour = GoldenHour.builder()
                .id(goldenHourId)
                .name(updateRequest.getName())
                .multiplier(updateRequest.getMultiplier())
                .startTime(updateRequest.getStartTime().toLocalTime())
                .endTime(updateRequest.getEndTime().toLocalTime())
                .isActive(updateRequest.getIsActive())
                .build();

        GoldenHourDTO expectedDTO = GoldenHourDTO.builder()
                .id(goldenHourId)
                .name("Updated Golden Hour")
                .multiplier(3.0)
                .startTime(now.minusHours(2))
                .endTime(now.plusHours(2))
                .isActive(true)
                .build();

        when(goldenHourRepository.findById(goldenHourId)).thenReturn(Optional.of(existingGoldenHour));
        when(goldenHourRepository.save(any(GoldenHour.class))).thenReturn(updatedGoldenHour);
        when(goldenHourMapper.toDTO(updatedGoldenHour)).thenReturn(expectedDTO);

        // When
        GoldenHourDTO result = goldenHourService.updateGoldenHour(goldenHourId, updateRequest);

        // Then
        assertThat(result).isEqualTo(expectedDTO);
        verify(goldenHourRepository).save(any(GoldenHour.class));
    }
}