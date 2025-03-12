package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

/**
 * Repository test class for SpinHistory entity.
 * Tests the data access operations for spin history records including:
 * - Pagination and filtering by participant event
 * - Time-based queries
 * - Statistical aggregations
 * - Status-based queries
 */
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

    /**
     * Sets up the test environment before each test.
     * Loads test data from the database and creates a new SpinHistory instance
     * with references to existing entities for testing repository operations.
     */
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

    /**
     * Tests finding spin histories by participant event with pagination.
     * Verifies that:
     * - The result is not empty
     * - The returned spin histories are associated with the correct participant event
     */
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

    /**
     * Tests finding spin histories within a specific time range.
     * Verifies that:
     * - The result is not empty
     * - The spin times of returned records are within the specified range
     */
    @Test
    void whenFindByTimeRange_thenReturnSet() {
        // Given
        spinHistory = spinHistoryRepository.save(spinHistory);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // When
        Set<SpinHistory> result = spinHistoryRepository
            .findAllByParticipantEventIdAndSpinTimeBetween(
                participantEvent.getId(), 
                start, 
                end
            );

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.iterator().next().getSpinTime()).isBetween(start, end);
    }

    /**
     * Tests counting spins made today for a specific participant event.
     * Verifies that the count is positive after adding a spin history record.
     */
    @Test
    void whenCountTodaySpins_thenReturnCount() {
        // Given
        spinHistory = spinHistoryRepository.save(spinHistory);

        // When
        long count = spinHistoryRepository.countTodaySpins(participantEvent.getId());

        // Then
        assertThat(count).isPositive();
    }

    /**
     * Tests counting winning spins for a specific participant event.
     * Verifies that the count is positive after adding a winning spin history record.
     */
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

    /**
     * Tests retrieving aggregated spin statistics for a participant event.
     * Verifies that:
     * - The stats array has the expected size
     * - Each statistic (total spins, winning spins, total points, etc.) has the expected value
     */
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

    /**
     * Tests checking for unfinalized spins for a participant event.
     * Verifies that the method returns true when there are unfinalized spins.
     */
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
