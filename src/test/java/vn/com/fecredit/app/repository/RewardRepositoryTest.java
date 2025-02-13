package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Reward;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class RewardRepositoryTest {

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private EventRepository eventRepository;

    private Event event;
    private Reward activeReward;
    private Reward inactiveReward;
    private Reward futureReward;
    private Reward provinceReward;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setCode("TEST-EVENT-" + System.currentTimeMillis());
        event.setName("Test Event");
        event.setStartDate(now.minusDays(1));
        event.setEndDate(now.plusDays(1));
        event.setIsActive(true);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        event = eventRepository.save(event);

        activeReward = new Reward();
        activeReward.setEvent(event);
        activeReward.setName("Active Reward");
        activeReward.setQuantity(10);
        activeReward.setRemainingQuantity(5);
        activeReward.setIsActive(true);
        activeReward.setProbability(0.5);
        activeReward.setStartDate(now.minusDays(1));
        activeReward.setEndDate(now.plusDays(1));
        activeReward.setCreatedAt(now);
        activeReward.setUpdatedAt(now);
        activeReward = rewardRepository.save(activeReward);

        inactiveReward = new Reward();
        inactiveReward.setEvent(event);
        inactiveReward.setName("Inactive Reward");
        inactiveReward.setQuantity(10);
        inactiveReward.setRemainingQuantity(10);
        inactiveReward.setIsActive(false);
        inactiveReward.setProbability(0.3);
        inactiveReward.setCreatedAt(now);
        inactiveReward.setUpdatedAt(now);
        inactiveReward = rewardRepository.save(inactiveReward);

        futureReward = new Reward();
        futureReward.setEvent(event);
        futureReward.setName("Future Reward");
        futureReward.setQuantity(10);
        futureReward.setRemainingQuantity(10);
        futureReward.setIsActive(true);
        futureReward.setProbability(0.2);
        futureReward.setStartDate(now.plusDays(1));
        futureReward.setEndDate(now.plusDays(2));
        futureReward.setCreatedAt(now);
        futureReward.setUpdatedAt(now);
        futureReward = rewardRepository.save(futureReward);

        provinceReward = new Reward();
        provinceReward.setEvent(event);
        provinceReward.setName("Province Reward");
        provinceReward.setQuantity(10);
        provinceReward.setRemainingQuantity(10);
        provinceReward.setIsActive(true);
        provinceReward.setProbability(0.4);
        provinceReward.setApplicableProvinces("HN,HCM,DN");
        provinceReward.setCreatedAt(now);
        provinceReward.setUpdatedAt(now);
        provinceReward = rewardRepository.save(provinceReward);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    @Test
    void findByEventIdAndIsActiveTrue_ShouldReturnOnlyActiveRewards() {
        List<Reward> activeRewards = rewardRepository.findByEventIdAndIsActiveTrue(event.getId());

        assertThat(activeRewards).hasSize(3)
            .extracting("name")
            .containsExactlyInAnyOrder("Active Reward", "Future Reward", "Province Reward");
    }

    @Test
    void findActiveRewardsByEventId_ShouldReturnActiveRewardsWithQuantity() {
        List<Reward> activeRewards = rewardRepository.findActiveRewardsByEventId(event.getId());

        assertThat(activeRewards).hasSize(3)
            .extracting("name")
            .containsExactlyInAnyOrder("Active Reward", "Future Reward", "Province Reward");
    }

    @Test
    void findAvailableRewards_ShouldReturnRewardsInDateRangeWithQuantity() {
        List<Reward> available = rewardRepository.findAvailableRewards(event.getId(), now);

        assertThat(available).hasSize(2)
            .extracting("name")
            .containsExactlyInAnyOrder("Active Reward", "Province Reward");
    }

    @Test
    void findByEventIdAndProvinceAndIsActiveTrue_ShouldReturnMatchingRewards() {
        List<Reward> rewards = rewardRepository.findByEventIdAndProvinceAndIsActiveTrue(
            event.getId(), "HCM");

        assertThat(rewards).hasSize(1);
        assertThat(rewards.get(0).getName()).isEqualTo("Province Reward");
    }

    @Test
    void updateRemainingQuantity_ShouldUpdateQuantity() {
        int updated = rewardRepository.updateRemainingQuantity(activeReward.getId(), 4);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Reward updatedReward = rewardRepository.findById(activeReward.getId()).orElseThrow();
        assertThat(updated).isEqualTo(1);
        assertThat(updatedReward.getRemainingQuantity()).isEqualTo(4);
    }

    @Test
    void findAvailableRewardsOrderByProbability_ShouldReturnOrderedRewards() {
        List<Reward> rewards = rewardRepository.findAvailableRewardsOrderByProbability(event.getId());

        assertThat(rewards).hasSize(3);
        assertThat(rewards.get(0).getProbability()).isGreaterThanOrEqualTo(rewards.get(1).getProbability());
        assertThat(rewards.get(1).getProbability()).isGreaterThanOrEqualTo(rewards.get(2).getProbability());
    }
}