package vn.com.fecredit.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.exception.InvalidOperationException;
import vn.com.fecredit.app.mapper.SpinHistoryMapper;
import vn.com.fecredit.app.repository.ParticipantEventRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;

@ExtendWith(MockitoExtension.class)
class SpinHistoryServiceImplTest {

    @Mock
    private SpinHistoryRepository spinHistoryRepository;

    @Mock
    private ParticipantEventRepository participantEventRepository;

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private SpinHistoryMapper spinHistoryMapper;

    @InjectMocks
    private SpinHistoryServiceImpl spinHistoryService;

    private SpinHistory spinHistory;
    private ParticipantEvent participantEvent;
    private SpinHistoryDTO.CreateRequest createRequest;
    private SpinHistoryDTO.Response response;

    @BeforeEach
    void setUp() {
        Event event = Event.builder()
            .name("Test Event")
            .status(1)
            .build();

        EventLocation location = EventLocation.builder()
            .name("Test Location")
            .status(1)
            .event(event)
            .build();

        Participant participant = Participant.builder()
            .name("Test Participant")
            .status(1)
            .build();

        participantEvent = ParticipantEvent.builder()
            .event(event)
            .eventLocation(location)
            .participant(participant)
            .totalSpins(10)
            .remainingSpins(5)
            .dailySpinsUsed(0)
            .status(1)
            .build();

        spinHistory = SpinHistory.builder()
            .participantEvent(participantEvent)
            .spinTime(LocalDateTime.now())
            .finalized(false)
            .build();

        createRequest = SpinHistoryDTO.CreateRequest.builder()
            .participantEventId(1L)
            .build();

        response = SpinHistoryDTO.Response.builder()
            .id(1L)
            .participantEventId(1L)
            .build();
    }

    @Test
    void whenCreateSpin_thenSuccess() {
        // Given
        when(participantEventRepository.findById(1L)).thenReturn(Optional.of(participantEvent));
        when(spinHistoryMapper.toEntity(createRequest)).thenReturn(spinHistory);
        when(spinHistoryRepository.save(any(SpinHistory.class))).thenReturn(spinHistory);
        when(spinHistoryMapper.toResponse(spinHistory)).thenReturn(response);

        // When
        SpinHistoryDTO.Response result = spinHistoryService.createSpin(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(spinHistoryRepository).save(any(SpinHistory.class));
    }

    @Test
    void whenCreateSpinWithInvalidParticipantEvent_thenThrowException() {
        // Given
        participantEvent.setStatus(0);
        when(participantEventRepository.findById(1L)).thenReturn(Optional.of(participantEvent));

        // When/Then
        assertThatThrownBy(() -> spinHistoryService.createSpin(createRequest))
            .isInstanceOf(InvalidOperationException.class)
            .hasMessageContaining("Cannot create new spin");
    }

    @Test
    void whenRecordWin_thenSuccess() {
        // Given
        Reward reward = Reward.builder().id(1L).build();
        when(spinHistoryRepository.findById(1L)).thenReturn(Optional.of(spinHistory));
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(reward));
        when(spinHistoryRepository.save(any(SpinHistory.class))).thenReturn(spinHistory);
        when(spinHistoryMapper.toResponse(spinHistory)).thenReturn(response);

        // When
        SpinHistoryDTO.Response result = spinHistoryService.recordWin(1L, 1L, 100);

        // Then
        assertThat(result).isNotNull();
        verify(spinHistoryRepository).save(any(SpinHistory.class));
    }

    @Test
    void whenFindAllByParticipantEvent_thenReturnPage() {
        // Given
        Page<SpinHistory> page = new PageImpl<>(List.of(spinHistory));
        when(spinHistoryRepository.findAllByParticipantEventId(1L, Pageable.unpaged()))
            .thenReturn(page);
        when(spinHistoryMapper.toResponse(spinHistory)).thenReturn(response);

        // When
        Page<SpinHistoryDTO.Response> result = spinHistoryService
            .findAllByParticipantEvent(1L, Pageable.unpaged());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void whenGetStatistics_thenReturnStats() {
        // Given
        when(participantEventRepository.findById(1L)).thenReturn(Optional.of(participantEvent));
        SpinHistoryDTO.Statistics stats = SpinHistoryDTO.Statistics.builder()
            .participantEventId(1L)
            .totalSpins(5)
            .winningSpins(2)
            .build();
        when(spinHistoryMapper.toStatistics(participantEvent)).thenReturn(stats);

        // When
        SpinHistoryDTO.Statistics result = spinHistoryService.getParticipantEventStatistics(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalSpins()).isEqualTo(5);
        assertThat(result.getWinningSpins()).isEqualTo(2);
    }

    @Test
    void whenGetNonExistentSpin_thenThrowException() {
        // Given
        when(spinHistoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> spinHistoryService.getById(999L))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenFinalizeSpin_thenSuccess() {
        // Given
        when(spinHistoryRepository.findById(1L)).thenReturn(Optional.of(spinHistory));
        when(spinHistoryRepository.save(any(SpinHistory.class))).thenReturn(spinHistory);
        when(spinHistoryMapper.toResponse(spinHistory)).thenReturn(response);

        // When
        SpinHistoryDTO.Response result = spinHistoryService.finalizeSpin(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(spinHistory.isFinalized()).isTrue();
        verify(spinHistoryRepository).save(spinHistory);
    }
}
