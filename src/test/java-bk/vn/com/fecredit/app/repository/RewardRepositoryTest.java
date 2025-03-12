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

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Reward;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RewardRepositoryTest {

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private EventRepository eventRepository;

    private Event event;

    @BeforeEach
    void setUp() {
        event = Event.builder()
            .code("TEST-EVENT")
            .name("Test Event")
            .totalSpins(1000L)
            .remainingSpins(1000L)
            .isActive(true)
            .build();
        event = eventRepository.save(event);
    }

    @Test
    void findByEventIdAndIsActiveTrue_ShouldReturnOnlyActiveRewards() {
        // Given
        Reward activeReward = Reward.builder()
            .event(event)
            .name("Active Reward")
            .quantity(100)
            .remainingQuantity(100)
            .probability(0.5)
            .isActive(true)
            .build();
        activeReward.setApplicableProvincesFromString("HN,HCM");
        rewardRepository.save(activeReward);

        Reward inactiveReward = Reward.builder()
            .event(event)
            .name("Inactive Reward")
            .quantity(100)
            .remainingQuantity(100)
            .probability(0.5)
            .isActive(false)
            .build();
        inactiveReward.setApplicableProvincesFromString("HN,HCM");
        rewardRepository.save(inactiveReward);

        // When
        List<Reward> rewards = rewardRepository.findByEventIdAndIsActiveTrue(event.getId());

        // Then
        assertThat(rewards).hasSize(1);
        assertThat(rewards.get(0).getName()).isEqualTo("Active Reward");
    }

    @Test
    void findAvailableRewards_ShouldReturnRewardsWithinValidPeriod() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        Reward validReward = createReward("Valid Reward", now.minusDays(1), now.plusDays(1));
        Reward expiredReward = createReward("Expired Reward", now.minusDays(2), now.minusDays(1));
        Reward futureReward = createReward("Future Reward", now.plusDays(1), now.plusDays(2));

        // When
        List<Reward> rewards = rewardRepository.findAvailableRewards(event.getId(), now);

        // Then
        assertThat(rewards).hasSize(1);
        assertThat(rewards.get(0).getName()).isEqualTo("Valid Reward");
    }

    private Reward createReward(String name, LocalDateTime startDate, LocalDateTime endDate) {
        Reward reward = Reward.builder()
            .event(event)
            .name(name)
            .quantity(100)
            .remainingQuantity(100)
            .probability(0.5)
            .isActive(true)
            .startDate(startDate)
            .endDate(endDate)
            .build();
        reward.setApplicableProvincesFromString("HN,HCM");
        return rewardRepository.save(reward);
    }
}