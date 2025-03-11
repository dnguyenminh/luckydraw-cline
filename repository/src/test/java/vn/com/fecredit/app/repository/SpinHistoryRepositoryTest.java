package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;

@DataJpaTest
@Sql({"/schema-test.sql", "/data-test.sql"})
class SpinHistoryRepositoryTest {

    @Autowired
    private SpinHistoryRepository spinHistoryRepository;

    @Autowired
    private ParticipantEventRepository participantEventRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Autowired
    private RewardRepository rewardRepository;

    private Event event;
    private EventLocation location;
    private ParticipantEvent participantEvent;
    private Reward reward;
    private SpinHistory spinHistory;

    @BeforeEach
    void setUp() {
        event = eventRepository.findById(1L).orElseThrow();
        location = eventLocationRepository.findById(1L).orElseThrow();
        participantEvent = participantEventRepository.findById(1L).orElseThrow();
        reward = rewardRepository.findById(1L).orElseThrow();

        spinHistory = SpinHistory.builder()
                .participantEvent(participantEvent)
                .spinTime(LocalDateTime.now())
                .win(false)
                .finalized(false)
                .status(1)
                .build();
    }

    @Test
    void whenFindByParticipantEvent_thenReturnPage() {
        // Given
        spinHistory = spinHistoryRepository.save(spinHistory);

        // When
        Page<SpinHistory> result = spinHistoryRepository.findAllByParticipantEventId(
            participantEvent.getId(), 
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getParticipantEvent().getId())
            .isEqualTo(participantEvent.getId());
    }

    @Test
    void whenFindByTimeRange_thenReturnList() {
        // Given
        spinHistory = spinHistoryRepository.save(spinHistory);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // When
        List<SpinHistory> result = spinHistoryRepository
            .findAllByParticipantEventIdAndSpinTimeBetween(
                participantEvent.getId(), 
                start, 
                end
            );

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getSpinTime()).isBetween(start, end);
    }

    @Test
    void whenCountTodaySpins_thenReturnCount() {
        // Given
        spinHistory = spinHistoryRepository.save(spinHistory);

        // When
        long count = spinHistoryRepository.countTodaySpins(participantEvent.getId());

        // Then
        assertThat(count).isPositive();
    }

    @Test
    void whenCountWinningSpins_thenReturnCount() {
        // Given
        spinHistory.setWin(true);
        spinHistory = spinHistoryRepository.save(spinHistory);

        // When
        long count = spinHistoryRepository.countWinningSpins(participantEvent.getId());

        // Then
        assertThat(count).isPositive();
    }

    @Test
    void whenGetSpinStats_thenReturnStats() {
        // Given
        spinHistory.setWin(true);
        spinHistory.setPointsEarned(100);
        spinHistory = spinHistoryRepository.save(spinHistory);

        // When
        Object[] stats = spinHistoryRepository.getSpinStats(participantEvent.getId())
            .orElseThrow();

        // Then
        assertThat(stats).hasSize(5);
        assertThat((Long) stats[0]).isPositive(); // totalSpins
        assertThat((Long) stats[1]).isPositive(); // winningSpins
        assertThat((Integer) stats[2]).isEqualTo(100); // totalPoints
        assertThat((LocalDateTime) stats[3]).isNotNull(); // firstSpinTime
        assertThat((LocalDateTime) stats[4]).isNotNull(); // lastSpinTime
    }

    @Test
    void whenFindUnfinalizedSpins_thenReturnTrue() {
        // Given
        spinHistory = spinHistoryRepository.save(spinHistory);

        // When
        boolean hasUnfinalized = spinHistoryRepository
            .existsByParticipantEventIdAndFinalizedFalse(participantEvent.getId());

        // Then
        assertThat(hasUnfinalized).isTrue();
    }
}
