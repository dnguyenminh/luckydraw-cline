package vn.com.fecredit.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.exception.InvalidOperationException;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.ParticipantEventRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.service.SpinHistoryService;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema-test.sql", "/data-test.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class SpinHistoryServiceIntegrationTest {

    @Autowired
    private SpinHistoryService spinHistoryService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private ParticipantEventRepository participantEventRepository;

    @Autowired
    private RewardRepository rewardRepository;

    private Event event;
    private EventLocation location;
    private Participant participant;
    private ParticipantEvent participantEvent;
    private Reward reward;

    @BeforeEach
    void setUp() {
        event = eventRepository.save(Event.builder()
            .name("Test Event")
            .code("TEST")
            .status(1)
            .build());

        location = eventLocationRepository.save(EventLocation.builder()
            .name("Test Location")
            .event(event)
            .status(1)
            .build());

        participant = participantRepository.save(Participant.builder()
            .name("Test Participant")
            .status(1)
            .build());

        participantEvent = participantEventRepository.save(ParticipantEvent.builder()
            .event(event)
            .eventLocation(location)
            .participant(participant)
            .totalSpins(10)
            .remainingSpins(10)
            .dailySpinsUsed(0)
            .status(1)
            .build());

        reward = rewardRepository.save(Reward.builder()
            .name("Test Reward")
            .event(event)
            .status(1)
            .build());
    }

    @Test
    void fullSpinCycle_Success() {
        // Create spin
        SpinHistoryDTO.CreateRequest createRequest = SpinHistoryDTO.CreateRequest.builder()
            .participantEventId(participantEvent.getId())
            .build();

        SpinHistoryDTO.Response response = spinHistoryService.createSpin(createRequest);
        assertThat(response).isNotNull();
        assertThat(response.getParticipantEventId()).isEqualTo(participantEvent.getId());

        // Record win
        response = spinHistoryService.recordWin(response.getId(), reward.getId(), 100);
        assertThat(response).isNotNull();
        assertThat(response.getRewardId()).isEqualTo(reward.getId());
        assertThat(response.getPointsEarned()).isEqualTo(100);
        assertThat(response.getWin()).isTrue();

        // Finalize spin
        response = spinHistoryService.finalizeSpin(response.getId());
        assertThat(response).isNotNull();
        assertThat(response.getFinalized()).isTrue();

        // Verify participant event stats
        SpinHistoryDTO.Statistics stats = spinHistoryService.getParticipantEventStatistics(participantEvent.getId());
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalSpins()).isEqualTo(1);
        assertThat(stats.getWinningSpins()).isEqualTo(1);
        assertThat(stats.getTotalPoints()).isEqualTo(100);
    }

    @Test
    void whenCreateMultipleSpins_thenRespectDailyLimit() {
        SpinHistoryDTO.CreateRequest createRequest = SpinHistoryDTO.CreateRequest.builder()
            .participantEventId(participantEvent.getId())
            .build();

        // Create spins up to daily limit
        for (int i = 0; i < 10; i++) {
            SpinHistoryDTO.Response response = spinHistoryService.createSpin(createRequest);
            assertThat(response).isNotNull();
        }

        // Attempt to create one more spin
        assertThatThrownBy(() -> spinHistoryService.createSpin(createRequest))
            .isInstanceOf(InvalidOperationException.class)
            .hasMessageContaining("daily limit");
    }

    @Test
    void whenQueryHistory_thenReturnCorrectPage() {
        // Create some spins
        SpinHistoryDTO.CreateRequest createRequest = SpinHistoryDTO.CreateRequest.builder()
            .participantEventId(participantEvent.getId())
            .build();

        for (int i = 0; i < 5; i++) {
            spinHistoryService.createSpin(createRequest);
        }

        // Query with pagination
        Page<SpinHistoryDTO.Response> page = spinHistoryService
            .findAllByParticipantEvent(participantEvent.getId(), PageRequest.of(0, 3));

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    @Test
    void whenInvalidId_thenThrowException() {
        assertThatThrownBy(() -> spinHistoryService.getById(999L))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void whenFindByTimeRange_thenReturnCorrectResults() {
        // Create some spins
        SpinHistoryDTO.CreateRequest createRequest = SpinHistoryDTO.CreateRequest.builder()
            .participantEventId(participantEvent.getId())
            .build();

        for (int i = 0; i < 3; i++) {
            spinHistoryService.createSpin(createRequest);
        }

        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        var spins = spinHistoryService.findSpinsByTimeRange(participantEvent.getId(), start, end);
        assertThat(spins).hasSize(3);
    }

    @Test
    void whenGetTodaySpins_thenReturnCorrectResults() {
        // Create some spins
        SpinHistoryDTO.CreateRequest createRequest = SpinHistoryDTO.CreateRequest.builder()
            .participantEventId(participantEvent.getId())
            .build();

        for (int i = 0; i < 3; i++) {
            spinHistoryService.createSpin(createRequest);
        }

        var todaySpins = spinHistoryService.findTodaysSpins(participantEvent.getId());
        assertThat(todaySpins).hasSize(3);
    }
}
