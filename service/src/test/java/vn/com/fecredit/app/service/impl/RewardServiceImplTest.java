package vn.com.fecredit.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.common.EntityStatus;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.exception.InvalidOperationException;
import vn.com.fecredit.app.mapper.RewardMapper;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.RewardRepository;

@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    private RewardMapper rewardMapper;

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private EventLocationRepository eventLocationRepository;

    @Mock
    private EventRepository eventRepository;

    private RewardServiceImpl rewardService;

    @Captor
    private ArgumentCaptor<Reward> rewardCaptor;

    private RewardDTO.Response rewardResponse;
    private RewardDTO.Summary rewardSummary;
    private RewardDTO.CreateRequest createRequest;
    private RewardDTO.UpdateRequest updateRequest;
    private Reward testReward;
    private EventLocation testLocation;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        rewardService = new RewardServiceImpl(
            rewardRepository,
            eventLocationRepository,
            eventRepository,
            rewardMapper
        );

        testEvent = Event.builder()
            .id(1L)
            .code("EVENT001")
            .status(EntityStatus.ACTIVE.getValue())
            .build();

        testLocation = EventLocation.builder()
            .id(1L)
            .code("LOC001")
            .event(testEvent)
            .status(EntityStatus.ACTIVE.getValue())
            .build();

        testReward = Reward.builder()
            .id(1L)
            .code("REWARD001")
            .name("Test Reward")
            .description("Test Description")
            .totalQuantity(100)
            .remainingQuantity(100)
            .winProbability(0.1)
            .eventLocation(testLocation)
            .status(EntityStatus.ACTIVE.getValue())
            .build();

        when(rewardRepository.findByLocationAndStatus(any(), anyInt()))
            .thenReturn(new ArrayList<>());

        initializeDTOs();
    }

    private void initializeDTOs() {
        createRequest = RewardDTO.CreateRequest.builder()
                .code("REWARD002")
                .name("New Test Reward")
                .description("New Test Description")
                .initialQuantity(100)
                .winProbability(0.1)
                .eventLocationId(1L)
                .build();

        updateRequest = RewardDTO.UpdateRequest.builder()
                .name("Updated Reward")
                .description("Updated Description")
                .remainingQuantity(90)
                .winProbability(0.2)
                .active(true)
                .build();

        rewardResponse = RewardDTO.Response.builder()
                .id(1L)
                .code("REWARD001")
                .name("Test Reward")
                .description("Test Description")
                .initialQuantity(100)
                .remainingQuantity(100)
                .winProbability(0.1)
                .active(true)
                .eventLocationId(1L)
                .build();

        rewardSummary = RewardDTO.Summary.builder()
                .id(1L)
                .code("REWARD001")
                .name("Test Reward")
                .remainingQuantity(100)
                .winProbability(0.1)
                .active(true)
                .build();
    }

    @Test
    void getById_WhenExists_ShouldReturnReward() {
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(testReward));
        
        Reward result = rewardService.getById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("REWARD001");
    }

    @Test
    void getById_WhenNotFound_ShouldThrowException() {
        when(rewardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rewardService.getById(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Reward not found with id: 999");
    }

    @Nested
    class CreateOperations {
        @Test
        void createReward_ShouldCreateAndReturnReward() {
            EventLocation location = EventLocation.builder()
                .id(1L)
                .status(EntityStatus.ACTIVE.getValue())
                .build();

            Reward newReward = Reward.builder()
                .code(createRequest.getCode())
                .name(createRequest.getName())
                .description(createRequest.getDescription())
                .totalQuantity(createRequest.getInitialQuantity())
                .remainingQuantity(createRequest.getInitialQuantity())
                .winProbability(createRequest.getWinProbability())
                .status(EntityStatus.ACTIVE.getValue())
                .build();

            when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(location));
            when(rewardMapper.toEntity(createRequest)).thenReturn(newReward);
            when(rewardRepository.save(any(Reward.class))).thenReturn(newReward);
            when(rewardMapper.toResponse(any(Reward.class))).thenReturn(rewardResponse);

            RewardDTO.Response result = rewardService.create(createRequest);

            assertThat(result).isNotNull();
            verify(rewardRepository).save(rewardCaptor.capture());
            Reward savedReward = rewardCaptor.getValue();
            assertThat(savedReward.getStatus()).isEqualTo(EntityStatus.ACTIVE.getValue());
            assertThat(savedReward.getEventLocation()).isEqualTo(location);
        }
    }

    @Nested
    class ValidationOperations {
        @Test
        void validateReward_WithValidReward_ShouldSucceed() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
            when(rewardRepository.findById(1L)).thenReturn(Optional.of(testReward));

            rewardService.validateReward(1L, 1L);
            
            verify(eventRepository).findById(1L);
            verify(rewardRepository).findById(1L);
        }

        @Test
        void validateReward_WithWrongEvent_ShouldThrowException() {
            when(eventRepository.findById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rewardService.validateReward(2L, 1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Event not found with id: 2");
                
            verify(eventRepository).findById(2L);
            verifyNoInteractions(rewardRepository);
        }

        @Test 
        void validateReward_WithInactiveReward_ShouldThrowException() {
            testReward.setStatus(EntityStatus.INACTIVE.getValue());
            when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
            when(rewardRepository.findById(1L)).thenReturn(Optional.of(testReward));

            assertThatThrownBy(() -> rewardService.validateReward(1L, 1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Reward is not active");
        }
    }
}
