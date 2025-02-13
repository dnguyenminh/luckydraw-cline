package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

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

    private GoldenHourMapper goldenHourMapper;
    private RewardMapper rewardMapper;
    private RewardService rewardService;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        // Initialize mappers in correct order due to dependency
        goldenHourMapper = new GoldenHourMapper();
        rewardMapper = new RewardMapper(goldenHourMapper);

        rewardService = new RewardService(
                rewardRepository,
                eventRepository,
                goldenHourRepository,
                rewardMapper,
                goldenHourMapper);
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

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        Reward reward = rewardMapper.toEntity(createRequest);
        reward.setEvent(event);
        reward.setId(1L);
        reward.setRemainingQuantity(createRequest.getQuantity());

        when(rewardRepository.save(any(Reward.class))).thenReturn(reward);

        // When
        RewardDTO result = rewardService.createReward(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(createRequest.getName());
        assertThat(result.getQuantity()).isEqualTo(createRequest.getQuantity());
        assertThat(result.getProbability()).isEqualTo(createRequest.getProbability());
        assertThat(result.getEventId()).isEqualTo(eventId);
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

        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(existingReward));

        Reward updatedReward = existingReward;
        updatedReward.setName(updateRequest.getName());
        updatedReward.setQuantity(updateRequest.getQuantity());
        updatedReward.setProbability(updateRequest.getProbability());

        when(rewardRepository.save(any(Reward.class))).thenReturn(updatedReward);

        // When
        RewardDTO result = rewardService.updateReward(rewardId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(updateRequest.getName());
        assertThat(result.getQuantity()).isEqualTo(updateRequest.getQuantity());
        assertThat(result.getProbability()).isEqualTo(updateRequest.getProbability());
        verify(rewardRepository).save(any(Reward.class));
    }

    @Test
    void addGoldenHour_ShouldAddAndReturnUpdatedReward() {
        // Given
        Long rewardId = 1L;

        // Create first golden hour
        GoldenHourDTO.CreateRequest createRequest1 = GoldenHourDTO.CreateRequest.builder()
                .name("Morning Golden Hour")
                .multiplier(2.0)
                .startTime(now.withHour(9).withMinute(0))
                .endTime(now.withHour(10).withMinute(0))
                .isActive(true)
                .build();

        // Create second golden hour
        GoldenHourDTO.CreateRequest createRequest2 = GoldenHourDTO.CreateRequest.builder()
                .name("Evening Golden Hour")
                .multiplier(3.0)
                .startTime(now.withHour(17).withMinute(0))
                .endTime(now.withHour(18).withMinute(0))
                .isActive(true)
                .build();

        Reward reward = Reward.builder()
                .id(rewardId)
                .name("Test Reward")
                .goldenHours(new HashSet<>())
                .build();

        // First golden hour
        GoldenHour goldenHour1 = goldenHourMapper.createEntity(createRequest1);
        goldenHour1.setId(1L);
        // goldenHour1.setReward(reward);
        // reward.addGoldenHour(goldenHour1);

        // Second golden hour
        GoldenHour goldenHour2 = goldenHourMapper.createEntity(createRequest2);
        goldenHour2.setId(2L);
        // goldenHour2.setReward(reward);
        // reward.addGoldenHour(goldenHour2);

        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(reward));
        when(goldenHourRepository.save(any(GoldenHour.class))).thenReturn(goldenHour1, goldenHour2);

        // When
        rewardService.addGoldenHour(rewardId, createRequest1);
        RewardDTO result2 = rewardService.addGoldenHour(rewardId, createRequest2);

        // Then
        assertThat(result2).isNotNull();
        assertThat(result2.getName()).isEqualTo(reward.getName());
        assertThat(result2.getGoldenHours()).hasSize(2);

        // Verify first golden hour
        assertThat(result2.getGoldenHours().get(0).getName()).isEqualTo(createRequest1.getName());
        assertThat(result2.getGoldenHours().get(0).getMultiplier()).isEqualTo(createRequest1.getMultiplier());

        // Verify second golden hour
        assertThat(result2.getGoldenHours().get(1).getName()).isEqualTo(createRequest2.getName());
        assertThat(result2.getGoldenHours().get(1).getMultiplier()).isEqualTo(createRequest2.getMultiplier());

        verify(goldenHourRepository, times(2)).save(any(GoldenHour.class));
    }
}