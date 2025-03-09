package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.BaseRepositoryTest;

class SpinHistoryRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private SpinHistoryRepository spinHistoryRepository;

    private Event event;
    private EventLocation location;
    private Region region;
    private Participant participant;
    private Reward reward;
    private GoldenHour goldenHour;

    @BeforeEach
    void setUp() {
        // Create and save test Region
        region = Region.builder()
            .name("Test Region")
            .code("TEST_REG002") // Different from NORTH, CENTRAL, SOUTH
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(region);

        // Create and save test Event
        event = Event.builder()
            .name("Test Event")
            .code("EVENT_TEST002") // Different from TET2024, SUMMER2024
            .startTime(LocalDateTime.now().minusDays(1))
            .endTime(LocalDateTime.now().plusDays(30))
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(event);

        // Create and save test EventLocation
        location = EventLocation.builder()
            .event(event)
            .region(region)
            .name("Test Location")
            .code("LOC_TEST002") // Different from HN-TET etc
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(location);

        // Create and save test Participant
        participant = Participant.builder()
            .name("Test Participant")
            .code("TEST_USER004") // Different from USER001 etc
            .account("TEST_USER004")
            .phone("0123456789")
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(participant);

        // Create and save test Reward
        reward = Reward.builder()
            .eventLocation(location)
            .name("Test Reward")
            .code("TEST_REWARD001") // Different from REWARD001 etc
            .points(100)
            .pointsRequired(50)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(reward);

        // Create and save test GoldenHour
        goldenHour = GoldenHour.builder()
            .eventLocation(location)
            .name("Test Golden Hour")
            .startTime(LocalDateTime.now().plusHours(1))
            .endTime(LocalDateTime.now().plusHours(2))
            .winProbability(0.2)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(goldenHour);
    }

    @Test
    void findByParticipantSince_ShouldReturnCorrectHistory() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        SpinHistory spin1 = SpinHistory.builder()
            .participant(participant)
            .eventLocation(location)
            .reward(reward)
            .timestamp(LocalDateTime.now())
            .win(true)
            .pointsEarned(100)
            .pointsSpent(50)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(spin1);

        SpinHistory spin2 = SpinHistory.builder()
            .participant(participant)
            .eventLocation(location)
            .timestamp(LocalDateTime.now().minusMinutes(30))
            .win(false)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();
        persistAndFlush(spin2);

        // When
        List<SpinHistory> history = spinHistoryRepository.findByParticipantSince(participant, since);

        // Then
        assertThat(history).hasSize(2);
        assertTrue(history.get(0).isWin()); // Most recent first
    }

    @Test
    void countWinsByLocationSince_ShouldReturnCorrectCount() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        SpinHistory win1 = SpinHistory.builder()
            .participant(participant)
            .eventLocation(location)
            .reward(reward)
            .timestamp(LocalDateTime.now())
            .win(true)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        SpinHistory win2 = SpinHistory.builder()
            .participant(participant)
            .eventLocation(location)
            .reward(reward)
            .timestamp(LocalDateTime.now().minusMinutes(30))
            .win(true)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        SpinHistory loss = SpinHistory.builder()
            .participant(participant)
            .eventLocation(location)
            .timestamp(LocalDateTime.now().minusMinutes(45))
            .win(false)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .build();

        persistAndFlush(win1);
        persistAndFlush(win2);
        persistAndFlush(loss);

        // When
        long winCount = spinHistoryRepository.countWinsByLocationSince(location, since);

        // Then
        assertEquals(2, winCount);
    }

    @Test
    void findByLocationAndTimeRange_ShouldPaginateResults() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        for (int i = 0; i < 15; i++) {
            SpinHistory spin = SpinHistory.builder()
                .participant(participant)
                .eventLocation(location)
                .timestamp(LocalDateTime.now().minusMinutes(i * 2))
                .win(i % 2 == 0)
                .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
                .build();
            persistAndFlush(spin);
        }

        // When
        Page<SpinHistory> firstPage = spinHistoryRepository.findByLocationAndTimeRange(
            location,
            start,
            end,
            PageRequest.of(0, 10, Sort.by("timestamp").descending())
        );

        Page<SpinHistory> secondPage = spinHistoryRepository.findByLocationAndTimeRange(
            location,
            start,
            end,
            PageRequest.of(1, 10, Sort.by("timestamp").descending())
        );

        // Then
        assertEquals(10, firstPage.getContent().size());
        assertEquals(5, secondPage.getContent().size());
        assertEquals(15, firstPage.getTotalElements());
    }
}
