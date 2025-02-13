package vn.com.fecredit.app.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;

/**
 * Service responsible for selecting rewards in the lucky draw system.
 * This service implements a weighted random selection algorithm that:
 * - Ensures fair distribution of rewards based on their remaining quantities
 * - Supports golden hour multipliers to increase win probability
 * - Maintains thread safety for concurrent access
 * - Validates location-based restrictions
 */
@Service
public class RewardSelectionService {
    /** Random number generator for reward selection, shared instance for better performance */
    private final Random random = new Random();

    /**
     * Selects a random reward for a given spin attempt.
     * 
     * @param event The event context for this spin
     * @param rewards List of available rewards to choose from
     * @param remainingSpins Number of spins remaining in the event
     * @param goldenHour Optional golden hour that may affect win probability
     * @param customerLocation Location of the customer for validation
     * @return Optional containing selected reward if won, empty if no reward
     */
    @Transactional
    public Optional<Reward> selectReward(Event event, List<Reward> rewards, long remainingSpins,
            Optional<GoldenHour> goldenHour, String customerLocation) {
        
        if (remainingSpins <= 0) {
            return Optional.empty();
        }

        List<Reward> sortedRewards = sortAndValidateRewards(rewards);
        if (sortedRewards.isEmpty()) {
            return Optional.empty();
        }

        Optional<Reward> selectedReward = selectRandomReward(sortedRewards, remainingSpins, goldenHour);
        return selectedReward.flatMap(reward -> tryClaimReward(reward, customerLocation));
    }

    /**
     * Filters out rewards with zero quantity and sorts them by remaining quantity (descending).
     * This ensures rewards with higher quantities are considered first.
     * 
     * @param rewards List of rewards to sort and validate
     * @return Sorted and filtered list of rewards
     */
    private List<Reward> sortAndValidateRewards(List<Reward> rewards) {
        return rewards.stream()
                .filter(r -> r.getRemainingQuantity() > 0)
                .sorted((r1, r2) -> Integer.compare(
                        r2.getRemainingQuantity(),
                        r1.getRemainingQuantity()))
                .toList();
    }

    /**
     * Performs the random reward selection using a weighted probability system.
     * The selection process:
     * 1. Calculates win probability based on remaining rewards and golden hour
     * 2. Determines if this spin should win anything
     * 3. If won, selects a specific reward based on remaining quantities
     * 
     * @param sortedRewards List of valid rewards sorted by quantity
     * @param remainingSpins Number of spins remaining
     * @param goldenHour Optional golden hour multiplier
     * @return Selected reward if won, empty if no win
     */
    private Optional<Reward> selectRandomReward(List<Reward> sortedRewards, long remainingSpins, Optional<GoldenHour> goldenHour) {
        int totalRemainingRewards = calculateTotalRemainingRewards(sortedRewards);
        double multiplier = calculateGoldenHourMultiplier(goldenHour);
        double adjustedTotalWeight = multiplier * totalRemainingRewards;
        
        // Determine if this spin wins anything based on adjusted probability
        if (random.nextDouble() * remainingSpins > adjustedTotalWeight) {
            return Optional.empty();
        }

        // Select specific reward using weighted random selection
        double randomValue = random.nextDouble() * totalRemainingRewards;
        double currentWeight = 0.0;
        
        for (Reward reward : sortedRewards) {
            currentWeight += reward.getRemainingQuantity();
            if (randomValue <= currentWeight) {
                return Optional.of(reward);
            }
        }
        
        return Optional.empty();
    }

    /**
     * Calculates the total number of remaining rewards across all reward types.
     * 
     * @param rewards List of rewards to sum
     * @return Total remaining quantity of all rewards
     */
    private int calculateTotalRemainingRewards(List<Reward> rewards) {
        return rewards.stream()
                .mapToInt(Reward::getRemainingQuantity)
                .sum();
    }

    /**
     * Attempts to claim a selected reward in a thread-safe manner.
     * This method ensures that:
     * - Reward quantity is decremented atomically
     * - Location restrictions are validated
     * - No reward is given if quantity reaches zero during claim
     * 
     * @param reward The reward to claim
     * @param customerLocation Location to validate against
     * @return The claimed reward if successful, empty if failed
     */
    private Optional<Reward> tryClaimReward(Reward reward, String customerLocation) {
        synchronized (reward) {
            if (reward.getRemainingQuantity() > 0) {
                reward.setRemainingQuantity(reward.getRemainingQuantity() - 1);
                if (isLocationAllowed(reward, customerLocation)) {
                    return Optional.of(reward);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves the multiplier value from a golden hour if present.
     * A multiplier increases the probability of winning during special periods.
     * 
     * @param goldenHour Optional golden hour period
     * @return Multiplier value, or 1.0 if no golden hour
     */
    private double calculateGoldenHourMultiplier(Optional<GoldenHour> goldenHour) {
        return goldenHour.map(GoldenHour::getMultiplier).orElse(1.0);
    }

    /**
     * Validates if a reward can be given at the specified location.
     * This allows for implementation of location-based restrictions on rewards.
     * 
     * @param reward Reward to validate
     * @param customerLocation Location to check
     * @return true if location is allowed, false otherwise
     */
    private boolean isLocationAllowed(Reward reward, String customerLocation) {
        // Implement location-based validation logic here
        // For now, return true to allow all locations
        return true;
    }

    /**
     * Resets any internal caches or state.
     * Currently a no-op, kept for backward compatibility with tests.
     */
    public void resetCaches() {
        // No caches to reset anymore
    }
}