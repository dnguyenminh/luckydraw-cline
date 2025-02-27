package vn.com.fecredit.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Reward;

@ExtendWith(MockitoExtension.class)
class RewardSelectionServiceTest {

    private RewardSelectionService service;

    @BeforeEach
    void setUp() {
        service = new RewardSelectionService();
    }

    @Test
    void shouldSelectRewardBasedOnProbability() {
        // Setup
        Event event = Event.builder()
                .id(1L)
                .isActive(true)
                .build();

        Reward reward1 = Reward.builder()
                .id(1L)
                .probability(0.7)
                .remainingQuantity(10)
                .build();

        Reward reward2 = Reward.builder()
                .id(2L)
                .probability(0.3)
                .remainingQuantity(10)
                .build();

        List<Reward> rewards = Arrays.asList(reward1, reward2);

        // Execute multiple times to verify probability distribution
        int reward1Count = 0;
        int reward2Count = 0;
        int iterations = 1000;

        for (int i = 0; i < iterations; i++) {
            Reward selected = service.selectReward(
                event,
                rewards,
                1L,
                System.currentTimeMillis(),
                "TEST"
            );
            if (selected.getId().equals(1L)) {
                reward1Count++;
            } else {
                reward2Count++;
            }
        }

        // Verify - allow for some variance in probability
        double reward1Ratio = (double) reward1Count / iterations;
        double reward2Ratio = (double) reward2Count / iterations;

        assertTrue(Math.abs(reward1Ratio - 0.7) < 0.1, "Reward1 ratio should be close to 0.7");
        assertTrue(Math.abs(reward2Ratio - 0.3) < 0.1, "Reward2 ratio should be close to 0.3");
    }

    @Test
    void shouldReturnNullWhenNoRewardsAvailable() {
        // Setup
        Event event = Event.builder()
                .id(1L)
                .isActive(true)
                .build();

        // Execute
        Reward selected = service.selectReward(
            event,
            Collections.emptyList(),
            1L,
            System.currentTimeMillis(),
            "TEST"
        );

        // Verify
        assertNull(selected);
    }

    @Test
    void shouldSelectFromEligibleRewardsOnly() {
        // Setup
        Event event = Event.builder()
                .id(1L)
                .isActive(true)
                .build();

        Reward ineligibleReward = Reward.builder()
                .id(1L)
                .probability(0.5)
                .remainingQuantity(0) // No remaining quantity
                .build();

        Reward eligibleReward = Reward.builder()
                .id(2L)
                .probability(0.5)
                .remainingQuantity(10)
                .build();

        List<Reward> rewards = Arrays.asList(ineligibleReward, eligibleReward);

        // Execute
        Reward selected = service.selectReward(
            event,
            rewards,
            1L,
            System.currentTimeMillis(),
            "TEST"
        );

        // Verify
        assertNotNull(selected);
        assertEquals(eligibleReward.getId(), selected.getId());
    }

    @Test
    void shouldHandleNullProbabilities() {
        // Setup
        Event event = Event.builder()
                .id(1L)
                .isActive(true)
                .build();

        Reward reward1 = Reward.builder()
                .id(1L)
                .probability(null)
                .remainingQuantity(10)
                .build();

        Reward reward2 = Reward.builder()
                .id(2L)
                .probability(0.5)
                .remainingQuantity(10)
                .build();

        List<Reward> rewards = Arrays.asList(reward1, reward2);

        // Execute
        Reward selected = service.selectReward(
            event,
            rewards,
            1L,
            System.currentTimeMillis(),
            "TEST"
        );

        // Verify
        assertNotNull(selected);
        assertEquals(reward2.getId(), selected.getId());
    }

    @Test
    void shouldFilterEligibleRewards() {
        // Setup
        Event event = Event.builder()
                .id(1L)
                .isActive(true)
                .build();

        Reward reward1 = Reward.builder()
                .id(1L)
                .event(event)
                .remainingQuantity(10)
                .isActive(true)
                .build();

        Reward reward2 = Reward.builder()
                .id(2L)
                .event(event)
                .remainingQuantity(0)
                .isActive(true)
                .build();

        Reward reward3 = Reward.builder()
                .id(3L)
                .event(event)
                .remainingQuantity(10)
                .isActive(false)
                .build();

        List<Reward> rewards = Arrays.asList(reward1, reward2, reward3);

        // Execute
        List<Reward> eligibleRewards = service.filterEligibleRewards(rewards, event);

        // Verify
        assertEquals(1, eligibleRewards.size());
        assertEquals(reward1.getId(), eligibleRewards.get(0).getId());
    }
}