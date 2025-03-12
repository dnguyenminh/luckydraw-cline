package vn.com.fecredit.app.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.model.SpinHistory;

@DataJpaTest
class SpinHistoryRepositoryTest {

    @Autowired
    private SpinHistoryRepository spinHistoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private RewardRepository rewardRepository;

    private Event event;
    private Participant participant;
    private Reward reward;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        event = Event.builder()
                .code("TEST_EVENT")
                .name("Test Event")
                .isActive(true)
                .build();
        event = eventRepository.save(event);

        participant = Participant.builder()
                .event(event)
                .code("TEST_PART")
                .name("Test Participant")
                .remainingSpins(10)
                .isActive(true)
                .build();
        participant = participantRepository.save(participant);

        reward = Reward.builder()
                .event(event)
                .code("TEST_REWARD")
                .name("Test Reward")
                .remainingQuantity(100)
                .isActive(true)
                .build();
        reward = rewardRepository.save(reward);
    }

    @Test
    void shouldFindByParticipantId() {
        // Given
        SpinHistory spin1 = createSpinHistory(true, now.minusMinutes(5));
        SpinHistory spin2 = createSpinHistory(false, now);
        spinHistoryRepository.saveAll(List.of(spin1, spin2));

        // When
        List<SpinHistory> found = spinHistoryRepository.findByParticipantId(participant.getId());

        // Then
        assertEquals(2, found.size());
    }

    @Test
    void shouldFindByParticipantIdAndTimeRange() {
        // Given
        SpinHistory spin1 = createSpinHistory(true, now.minusHours(2));
        SpinHistory spin2 = createSpinHistory(true, now.minusHours(1));
        SpinHistory spin3 = createSpinHistory(false, now);
        spinHistoryRepository.saveAll(List.of(spin1, spin2, spin3));

        // When
        List<SpinHistory> found = spinHistoryRepository.findByParticipantIdAndTimeRange(
                participant.getId(), 
                now.minusHours(1).minusMinutes(5), 
                now.plusMinutes(5));

        // Then
        assertEquals(2, found.size());
    }

    @Test
    void shouldFindFirstByParticipantIdOrderBySpinTimeDesc() {
        // Given
        SpinHistory spin1 = createSpinHistory(true, now.minusHours(1));
        SpinHistory spin2 = createSpinHistory(false, now);
        spinHistoryRepository.saveAll(List.of(spin1, spin2));

        // When
        SpinHistory found = spinHistoryRepository.findFirstByParticipantIdOrderBySpinTimeDesc(participant.getId())
                .orElse(null);

        // Then
        assertNotNull(found);
        assertEquals(now, found.getSpinTime());
    }

    @Test
    void shouldFindWinningSpins() {
        // Given
        SpinHistory spin1 = createSpinHistory(true, now.minusHours(1));
        SpinHistory spin2 = createSpinHistory(false, now);
        SpinHistory spin3 = createSpinHistory(true, now.plusHours(1));
        spinHistoryRepository.saveAll(List.of(spin1, spin2, spin3));

        // When
        List<SpinHistory> found = spinHistoryRepository.findWinningSpins(event.getId());

        // Then
        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(SpinHistory::getWon));
    }

    @Test
    void shouldCountWinningSpinsByEventId() {
        // Given
        SpinHistory spin1 = createSpinHistory(true, now.minusHours(1));
        SpinHistory spin2 = createSpinHistory(false, now);
        SpinHistory spin3 = createSpinHistory(true, now.plusHours(1));
        spinHistoryRepository.saveAll(List.of(spin1, spin2, spin3));

        // When
        long count = spinHistoryRepository.countWinningSpinsByEventId(event.getId());

        // Then
        assertEquals(2, count);
    }

    @Test
    void shouldCountWinningSpinsByEventIdAndRewardId() {
        // Given
        SpinHistory spin1 = createSpinHistory(true, now.minusHours(1));
        SpinHistory spin2 = createSpinHistory(false, now);
        spinHistoryRepository.saveAll(List.of(spin1, spin2));

        // When
        long count = spinHistoryRepository.countWinningSpinsByEventIdAndRewardId(event.getId(), reward.getId());

        // Then
        assertEquals(1, count);
    }

    @Test
    void shouldCheckHasSpinAfterTime() {
        // Given
        SpinHistory spin = createSpinHistory(true, now);
        spinHistoryRepository.save(spin);

        // When
        boolean hasAfter = spinHistoryRepository.hasSpinAfterTime(participant.getId(), now.minusMinutes(5));
        boolean hasBefore = spinHistoryRepository.hasSpinAfterTime(participant.getId(), now.plusMinutes(5));

        // Then
        assertTrue(hasAfter);
        assertFalse(hasBefore);
    }

    @Test
    void shouldFindWinningSpinsWithPagination() {
        // Given
        SpinHistory spin1 = createSpinHistory(true, now.minusHours(2));
        SpinHistory spin2 = createSpinHistory(true, now.minusHours(1));
        SpinHistory spin3 = createSpinHistory(false, now);
        spinHistoryRepository.saveAll(List.of(spin1, spin2, spin3));

        // When
        Page<SpinHistory> page = spinHistoryRepository.findWinningSpinsByEventId(
                event.getId(), 
                PageRequest.of(0, 10));

        // Then
        assertEquals(2, page.getContent().size());
        assertTrue(page.getContent().stream().allMatch(SpinHistory::getWon));
    }

    private SpinHistory createSpinHistory(boolean won, LocalDateTime spinTime) {
        return spinHistoryRepository.save(SpinHistory.builder()
                .event(event)
                .participant(participant)
                .reward(won ? reward : null)
                .spinTime(spinTime)
                .won(won)
                .isGoldenHour(false)
                .currentMultiplier(1.0)
                .isActive(true)
                .build());
    }
}