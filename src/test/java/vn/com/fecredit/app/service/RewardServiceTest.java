package vn.com.fecredit.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.mapper.GoldenHourMapper;
import vn.com.fecredit.app.mapper.RewardMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.GoldenHourRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RewardServiceTest {

    @Mock
    private RewardRepository rewardRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private SpinHistoryRepository spinHistoryRepository;
    @Mock 
    private GoldenHourService goldenHourService;
    @Mock
    private GoldenHourRepository goldenHourRepository;
    @Mock
    private RewardMapper rewardMapper;
    @Mock
    private GoldenHourMapper goldenHourMapper;

    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        rewardService = new RewardService(
            rewardRepository,
            eventRepository,
            spinHistoryRepository,
            goldenHourService,
            goldenHourRepository,
            rewardMapper,
            goldenHourMapper
        );
    }

    @Test
    void createReward_ShouldCreateAndReturnReward() {
        // Given
        Long eventId = 1L;
        RewardDTO.CreateRewardRequest createRequest = RewardDTO.CreateRewardRequest.builder()
                .name("Test Reward")
                .quantity(100)
                .probability(0.5)
                .isActive(true)
                .eventId(eventId)
                .build();

        Event event = Event.builder()
                .id(eventId)
                .name("Test Event")
                .build();

        Reward reward = Reward.builder()
                .id(1L)
                .name("Test Reward")
                .quantity(100)
                .remainingQuantity(100)
                .probability(0.5)
                .isActive(true)
                .event(event)
                .build();

        RewardDTO expectedRewardDTO = RewardDTO.builder()
                .id(1L)
                .name("Test Reward")
                .quantity(100)
                .remainingQuantity(100)
                .probability(0.5)
                .isActive(true)
                .eventId(eventId)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(rewardMapper.toEntity(createRequest)).thenReturn(reward);
        when(rewardRepository.save(any(Reward.class))).thenReturn(reward);
        when(rewardMapper.toDTO(reward)).thenReturn(expectedRewardDTO);

        // When
        RewardDTO result = rewardService.createReward(createRequest);

        // Then
        assertThat(result).isEqualTo(expectedRewardDTO);
        verify(rewardRepository).save(any(Reward.class));
        verify(eventRepository).findById(eventId);
    }

    @Test
    void updateReward_ShouldUpdateAndReturnReward() {
        // Given
        Long rewardId = 1L;
        RewardDTO.UpdateRewardRequest updateRequest = RewardDTO.UpdateRewardRequest.builder()
                .name("Updated Reward")
                .quantity(50)
                .probability(0.3)
                .build();

        Reward existingReward = Reward.builder()
                .id(rewardId)
                .name("Test Reward")
                .quantity(100)
                .probability(0.5)
                .build();

        Reward updatedReward = Reward.builder()
                .id(rewardId)
                .name("Updated Reward")
                .quantity(50)
                .probability(0.3)
                .build();

        RewardDTO expectedRewardDTO = RewardDTO.builder()
                .id(rewardId)
                .name("Updated Reward")
                .quantity(50)
                .probability(0.3)
                .build();

        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(existingReward));
        when(rewardRepository.save(any(Reward.class))).thenReturn(updatedReward);
        when(rewardMapper.toDTO(updatedReward)).thenReturn(expectedRewardDTO);

        // When
        RewardDTO result = rewardService.updateReward(rewardId, updateRequest);

        // Then
        assertThat(result).isEqualTo(expectedRewardDTO);
        verify(rewardRepository).save(any(Reward.class));
    }

    @Test
    void addGoldenHour_ShouldAddAndReturnUpdatedReward() {
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
                .goldenHours(new ArrayList<>())
                .build();

        GoldenHour newGoldenHour = GoldenHour.builder()
                .name(createRequest.getName())
                .multiplier(createRequest.getMultiplier())
                .startTime(createRequest.getStartTime().toLocalTime())
                .endTime(createRequest.getEndTime().toLocalTime())
                .isActive(createRequest.getIsActive())
                .reward(reward)
                .build();

        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(reward));
        when(goldenHourMapper.createEntity(createRequest)).thenReturn(newGoldenHour);
        when(rewardRepository.save(any(Reward.class))).thenReturn(reward);

        RewardDTO expectedRewardDTO = RewardDTO.builder()
                .id(rewardId)
                .name("Test Reward")
                .goldenHours(Collections.singletonList(
                    GoldenHourDTO.builder()
                        .id(1L)
                        .name("Test Golden Hour")
                        .multiplier(2.0)
                        .startTime(now.minusHours(1))
                        .endTime(now.plusHours(1))
                        .isActive(true)
                        .rewardId(rewardId)
                        .build()
                ))
                .build();

        when(rewardMapper.toDTO(any(Reward.class))).thenReturn(expectedRewardDTO);

        // When
        RewardDTO result = rewardService.addGoldenHour(rewardId, createRequest);

        // Then
        assertThat(result).isEqualTo(expectedRewardDTO);
        verify(rewardRepository).save(any(Reward.class));
        verify(goldenHourMapper).createEntity(createRequest);
    }
}