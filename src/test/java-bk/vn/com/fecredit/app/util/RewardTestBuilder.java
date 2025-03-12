package vn.com.fecredit.app.util;

import vn.com.fecredit.app.entity.Reward;

import java.time.LocalDateTime;
import java.util.UUID;

public class RewardTestBuilder {
    
    public static Reward createTestReward() {
        return createTestReward(EntityStatus.ACTIVE);
    }

    public static Reward createTestReward(EntityStatus status) {
        Reward reward = new Reward();
        reward.setName("Test Reward");
        reward.setCode("REW-" + UUID.randomUUID().toString().substring(0, 8));
        reward.setDescription("Test reward description");
        reward.setInitialQuantity(100);
        reward.setRemainingQuantity(100);
        reward.setWinProbability(0.1);
        reward.setPointsRequired(10);
        reward.setValidFrom(LocalDateTime.now());
        reward.setValidUntil(LocalDateTime.now().plusDays(30));
        reward.setStatus(status);
        return reward;
    }

    public static Reward createTestRewardWithCode(String code) {
        Reward reward = createTestReward();
        reward.setCode(code);
        return reward;
    }

    public static Reward createTestRewardWithQuantity(int initialQuantity) {
        Reward reward = createTestReward();
        reward.setInitialQuantity(initialQuantity);
        reward.setRemainingQuantity(initialQuantity);
        return reward;
    }

    public static Reward createTestRewardWithProbability(double probability) {
        Reward reward = createTestReward();
        reward.setWinProbability(probability);
        return reward;
    }

    public static Reward createTestRewardWithPoints(int points) {
        Reward reward = createTestReward();
        reward.setPointsRequired(points);
        return reward;
    }

    public static Reward createTestRewardWithValidity(LocalDateTime from, LocalDateTime until) {
        Reward reward = createTestReward();
        reward.setValidFrom(from);
        reward.setValidUntil(until);
        return reward;
    }

    public static Reward createTestRewardWithMetadata(String metadata) {
        Reward reward = createTestReward();
        reward.setMetadata(metadata);
        return reward;
    }

    public static Reward createExpiredReward() {
        Reward reward = createTestReward();
        reward.setValidFrom(LocalDateTime.now().minusDays(60));
        reward.setValidUntil(LocalDateTime.now().minusDays(30));
        return reward;
    }

    public static Reward createFutureReward() {
        Reward reward = createTestReward();
        reward.setValidFrom(LocalDateTime.now().plusDays(30));
        reward.setValidUntil(LocalDateTime.now().plusDays(60));
        return reward;
    }

    public static Reward createOutOfStockReward() {
        Reward reward = createTestReward();
        reward.setInitialQuantity(10);
        reward.setRemainingQuantity(0);
        return reward;
    }

    public static Reward createUnlimitedReward() {
        Reward reward = createTestReward();
        reward.setInitialQuantity(null);
        reward.setRemainingQuantity(null);
        return reward;
    }

    public static Reward createHighProbabilityReward() {
        Reward reward = createTestReward();
        reward.setWinProbability(0.9);
        return reward;
    }

    public static Reward createLowProbabilityReward() {
        Reward reward = createTestReward();
        reward.setWinProbability(0.1);
        return reward;
    }
}
