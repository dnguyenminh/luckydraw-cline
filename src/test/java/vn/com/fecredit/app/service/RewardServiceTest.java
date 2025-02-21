package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.mapper.RewardMapper;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.RewardRepository;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private GoldenHourRepository goldenHourRepository;

    @Mock
    private RewardMapper rewardMapper;

    @Mock
    private GoldenHourMapper goldenHourMapper;

    @InjectMocks
    private RewardService rewardService;

    private Reward reward;
    private GoldenHour goldenHour;
    private RewardDTO rewardDTO;
    private GoldenHourDTO.CreateRequest createRequest;

    @BeforeEach
    void setUp() {
        reward = new Reward();
        reward.setId(1L);
        reward.setName("Test Reward");

        goldenHour = new GoldenHour();
        goldenHour.setId(1L);
        goldenHour.setReward(reward);
        goldenHour.setStartHour(9);
        goldenHour.setEndHour(17);

        rewardDTO = new RewardDTO();
        rewardDTO.setId(1L);
        rewardDTO.setName("Test Reward");

        createRequest = new GoldenHourDTO.CreateRequest();
        createRequest.setStartHour(9);
        createRequest.setEndHour(17);
    }

    @Test
    void addGoldenHour_WhenRewardExists_ShouldAddGoldenHour() {
        // Given
        when(rewardRepository.findById(eq(1L))).thenReturn(Optional.of(reward));
        when(goldenHourMapper.toEntity(eq(createRequest))).thenReturn(goldenHour);
        when(rewardRepository.save(any(Reward.class))).thenReturn(reward);
        when(rewardMapper.toDTO(any(Reward.class))).thenReturn(rewardDTO);

        // When
        RewardDTO result = rewardService.addGoldenHour(1L, createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(goldenHourRepository).save(any(GoldenHour.class));
        verify(rewardRepository).save(any(Reward.class));
    }

    @Test
    void addGoldenHour_WhenRewardNotFound_ShouldThrowException() {
        // Given
        when(rewardRepository.findById(eq(99L))).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> rewardService.addGoldenHour(99L, createRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Reward not found with id: 99");
    }

    @Test
    void removeGoldenHour_WhenBothExist_ShouldRemoveGoldenHour() {
        // Given
        when(rewardRepository.findById(eq(1L))).thenReturn(Optional.of(reward));
        when(goldenHourRepository.findById(eq(1L))).thenReturn(Optional.of(goldenHour));
        when(rewardRepository.save(any(Reward.class))).thenReturn(reward);
        when(rewardMapper.toDTO(any(Reward.class))).thenReturn(rewardDTO);

        // When
        RewardDTO result = rewardService.removeGoldenHour(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        verify(goldenHourRepository).delete(any(GoldenHour.class));
        verify(rewardRepository).save(any(Reward.class));
    }

    @Test
    void removeGoldenHour_WhenGoldenHourNotBelongToReward_ShouldThrowException() {
        // Given
        Reward otherReward = new Reward();
        otherReward.setId(2L);
        
        GoldenHour otherGoldenHour = new GoldenHour();
        otherGoldenHour.setId(1L);
        otherGoldenHour.setReward(otherReward);

        when(rewardRepository.findById(eq(1L))).thenReturn(Optional.of(reward));
        when(goldenHourRepository.findById(eq(1L))).thenReturn(Optional.of(otherGoldenHour));

        // When/Then
        assertThatThrownBy(() -> rewardService.removeGoldenHour(1L, 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Golden hour does not belong to this reward");
    }
}