package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.model.SpinHistory;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
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
    private SpinHistory winSpin;
    private SpinHistory loseSpin;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        event = new Event();
        // Generate a unique event code to avoid duplicate key violations
        event.setCode("TEST-EVENT-" + System.currentTimeMillis());
        event.setName("Test Event");
        event.setStartDate(now.minusDays(1));
        event.setEndDate(now.plusDays(1));
        event.setIsActive(true);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        event = eventRepository.save(event);

        participant = new Participant();
        participant.setEvent(event);
        participant.setName("Test Participant");
        participant.setFullName("Test Participant Full"); // Set a non-null full_name
        participant.setEmployeeId("EMP123");
        participant.setCustomerId("CUST123");
        participant.setCardNumber("CARD123");
        participant.setEmail("test@example.com");
        participant.setIsActive(true);
        participant.setSpinsRemaining(5L);
        participant.setCreatedAt(now);
        participant.setUpdatedAt(now);
        participant = participantRepository.save(participant);

        reward = new Reward();
        reward.setEvent(event);
        reward.setName("Test Reward");
        reward.setQuantity(10);
        reward.setRemainingQuantity(5);
        reward.setActive(true);
        reward.setCreatedAt(now);
        reward.setUpdatedAt(now);
        reward = rewardRepository.save(reward);

        winSpin = new SpinHistory();
        winSpin.setEvent(event);
        winSpin.setParticipant(participant);
        winSpin.setReward(reward);
        winSpin.setSpinTime(now.minusMinutes(30));
        winSpin.setWon(true);
        winSpin.setCreatedAt(now);
        winSpin.setUpdatedAt(now);
        winSpin = spinHistoryRepository.save(winSpin);

        loseSpin = new SpinHistory();
        loseSpin.setEvent(event);
        loseSpin.setParticipant(participant);
        loseSpin.setSpinTime(now);
        loseSpin.setWon(false);
        loseSpin.setCreatedAt(now);
        loseSpin.setUpdatedAt(now);
        loseSpin = spinHistoryRepository.save(loseSpin);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    @Test
    void findByParticipantId_ShouldReturnAllSpins() {
        List<SpinHistory> spins = spinHistoryRepository.findByParticipantId(participant.getId());

        assertThat(spins).hasSize(2);
        assertThat(spins).extracting("participant.id")
            .containsOnly(participant.getId());
    }

    @Test
    void findByParticipantIdAndTimeRange_ShouldReturnSpinsInRange() {
        List<SpinHistory> spins = spinHistoryRepository.findByParticipantIdAndTimeRange(
            participant.getId(),
            now.minusMinutes(10),
            now.plusMinutes(10)
        );

        assertThat(spins).hasSize(1);
        // Use isCloseTo with a tolerance of 1 microsecond
        assertThat(spins.get(0).getSpinTime())
            .isCloseTo(now, within(1, ChronoUnit.MICROS));
    }

    @Test
    void countSpinsByParticipantAndTimeRange_ShouldReturnCorrectCount() {
        long count = spinHistoryRepository.countSpinsByParticipantAndTimeRange(
            participant.getId(),
            now.minusHours(1),
            now.plusHours(1)
        );

        assertThat(count).isEqualTo(2);
    }

    @Test
    void findByEventIdAndTimeRange_ShouldReturnSpinsInRange() {
        List<SpinHistory> spins = spinHistoryRepository.findByEventIdAndTimeRange(
            event.getId(),
            now.minusHours(1),
            now.plusHours(1)
        );

        assertThat(spins).hasSize(2);
        assertThat(spins.get(0).getSpinTime()).isAfter(spins.get(1).getSpinTime());
    }

    @Test
    void findWinningSpins_ShouldReturnOnlyWinningSpins() {
        List<SpinHistory> winningSpins = spinHistoryRepository.findWinningSpins(participant.getId());

        assertThat(winningSpins).hasSize(1);
        assertThat(winningSpins.get(0).getWon()).isTrue();
    }

    @Test
    void findByIdWithDetails_ShouldReturnSpinWithDetails() {
        Optional<SpinHistory> found = spinHistoryRepository.findByIdWithDetails(winSpin.getId());

        assertThat(found).isPresent();
        SpinHistory spin = found.get();
        assertThat(spin.getParticipant()).isNotNull();
        assertThat(spin.getReward()).isNotNull();
    }

    @Test
    void countWinningSpinsByEventId_ShouldReturnCorrectCount() {
        long count = spinHistoryRepository.countWinningSpinsByEventId(event.getId());

        assertThat(count).isEqualTo(1);
    }

    @Test
    void countWinningSpinsByEventIdAndRewardId_ShouldReturnCorrectCount() {
        long count = spinHistoryRepository.countWinningSpinsByEventIdAndRewardId(
            event.getId(),
            reward.getId()
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void findFirstByParticipantIdOrderBySpinTimeDesc_ShouldReturnLatestSpin() {
        Optional<SpinHistory> latestSpin = spinHistoryRepository
            .findFirstByParticipantIdOrderBySpinTimeDesc(participant.getId());

        assertThat(latestSpin).isPresent();
        // Compare spinTime with a tolerance of 1 microsecond
        assertThat(latestSpin.get().getSpinTime().truncatedTo(ChronoUnit.MICROS))
            .isCloseTo(now.truncatedTo(ChronoUnit.MICROS), within(1, ChronoUnit.MICROS));
    }

    @Test
    void hasSpinAfterTime_ShouldReturnTrue_WhenSpinExists() {
        boolean hasSpin = spinHistoryRepository.hasSpinAfterTime(
            participant.getId(),
            now.minusMinutes(5)
        );

        assertThat(hasSpin).isTrue();
    }

    @Test
    void hasSpinAfterTime_ShouldReturnFalse_WhenNoSpinExists() {
        boolean hasSpin = spinHistoryRepository.hasSpinAfterTime(
            participant.getId(),
            now.plusMinutes(5)
        );

        assertThat(hasSpin).isFalse();
    }
}