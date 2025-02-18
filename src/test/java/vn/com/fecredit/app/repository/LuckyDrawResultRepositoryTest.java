package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

import vn.com.fecredit.app.config.TestConfig;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.LuckyDrawResult;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.model.SpinHistory;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestConfig.class)
public class LuckyDrawResultRepositoryTest {
    


    @Autowired
    private LuckyDrawResultRepository luckyDrawResultRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private SpinHistoryRepository spinHistoryRepository;

    private Event event;
    private Participant participant;
    private Reward reward;
    private SpinHistory spinHistory;
    private LuckyDrawResult result;
    private final LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
    private final LocalDateTime futureEnd = futureStart.plusDays(7);

    @BeforeEach
    void setUp() {
        event = Event.builder()
            .code("TEST-EVENT-" + System.currentTimeMillis())
            .name("Test Event")
            .startDate(futureStart)
            .endDate(futureEnd)
            .isActive(true)
            .totalSpins(100L)
            .remainingSpins(100L)
            .createdAt(futureStart)
            .updatedAt(futureStart)
            .build();
        event = eventRepository.saveAndFlush(event);

        participant = new Participant();
        participant.setEvent(event);
        participant.setName("Test Participant");
        participant.setFullName("Test Participant");
        participant.setEmployeeId("EMP123");
        participant.setIsActive(true);
        participant.setCreatedAt(futureStart);
        participant.setUpdatedAt(futureStart);
        participant = participantRepository.save(participant);

        reward = new Reward();
        reward.setEvent(event);
        reward.setName("Test Reward");
        reward.setQuantity(10);
        reward.setRemainingQuantity(5);
        reward.setIsActive(true);
        reward.setCreatedAt(futureStart);
        reward.setUpdatedAt(futureStart);
        reward = rewardRepository.save(reward);

        spinHistory = new SpinHistory();
        spinHistory.setEvent(event);
        spinHistory.setParticipant(participant);
        spinHistory.setSpinTime(futureStart.plusHours(1));
        spinHistory.setWon(true);
        spinHistory.setCreatedAt(futureStart);
        spinHistory.setUpdatedAt(futureStart);
        spinHistory = spinHistoryRepository.save(spinHistory);

        result = new LuckyDrawResult();
        result.setEvent(event);
        result.setParticipant(participant);
        result.setReward(reward);
        result.setSpinHistory(spinHistory);
        result.setWinTime(futureStart.plusHours(1));
        result.setPackNumber(1);
        result.setIsClaimed(false);
        result = luckyDrawResultRepository.save(result);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    @Test
    void findByParticipantId_ShouldReturnResults() {
        List<LuckyDrawResult> results = luckyDrawResultRepository.findByParticipantId(participant.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getParticipant().getId()).isEqualTo(participant.getId());
    }

    @Test
    void findUnclaimedRewards_ShouldReturnUnclaimedResults() {
        List<LuckyDrawResult> unclaimed = luckyDrawResultRepository.findUnclaimedRewards(
            participant.getId(), reward.getId());

        assertThat(unclaimed).hasSize(1);
        assertThat(unclaimed.get(0).getIsClaimed()).isFalse();
    }

    @Test
    void findUnclaimedByRewardIdAndPackNumber_ShouldReturnResult() {
        Optional<LuckyDrawResult> found = luckyDrawResultRepository
            .findUnclaimedByRewardIdAndPackNumber(reward.getId(), 1);

        assertThat(found).isPresent();
        assertThat(found.get().getPackNumber()).isEqualTo(1);
    }

    @Test
    void countUnclaimedByRewardIdAndWinTimeBetween_ShouldReturnCount() {
        long count = luckyDrawResultRepository.countUnclaimedByRewardIdAndWinTimeBetween(
            reward.getId(), 
            futureStart,
            futureStart.plusHours(2)
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void findBySpinHistoryIdWithDetails_ShouldReturnResult() {
        Optional<LuckyDrawResult> found = luckyDrawResultRepository
            .findBySpinHistoryIdWithDetails(spinHistory.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSpinHistory().getId()).isEqualTo(spinHistory.getId());
    }

    @Test
    void claimReward_ShouldUpdateClaimStatus() {
        int updated = luckyDrawResultRepository.claimReward(
            result.getId(), "TEST_USER", "Test claim");
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Optional<LuckyDrawResult> found = luckyDrawResultRepository.findById(result.getId());
        assertThat(updated).isEqualTo(1);
        assertThat(found).isPresent();
        assertThat(found.get().getIsClaimed()).isTrue();
    }

    @Test
    void findPackNumbersByRewardId_ShouldReturnAllPackNumbers() {
        Set<Integer> packNumbers = luckyDrawResultRepository.findPackNumbersByRewardId(reward.getId());

        assertThat(packNumbers).hasSize(1);
        assertThat(packNumbers).contains(1);
    }

    @Test
    void findAvailablePacksForReward_ShouldReturnAvailablePacks() {
        List<Integer> availablePacks = luckyDrawResultRepository.findAvailablePacksForReward(
            reward.getId(), 10L, 5L);

        assertThat(availablePacks).hasSize(5);
        assertThat(availablePacks).doesNotContain(1);
    }

    @Test
    void existsByRewardIdAndPackNumber_ShouldReturnTrue_WhenExists() {
        boolean exists = luckyDrawResultRepository.existsByRewardIdAndPackNumber(
            reward.getId(), 1);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByRewardIdAndPackNumber_ShouldReturnFalse_WhenNotExists() {
        boolean exists = luckyDrawResultRepository.existsByRewardIdAndPackNumber(
            reward.getId(), 999);

        assertThat(exists).isFalse();
    }
}