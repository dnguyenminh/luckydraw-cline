package vn.com.fecredit.app.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;

@Service
@Slf4j
public class RewardSelectionService {
    private static final int MAX_REWARDS = 16;
    private static final double EARLY_PHASE = 0.5;  // First 50% of spins
    private static final double ENDGAME_THRESHOLD = 0.7; // Last 30% of spins
    
    private final ThreadLocal<AtomicInteger[]> quantityRefs = ThreadLocal.withInitial(() -> new AtomicInteger[MAX_REWARDS]);
    private final ThreadLocal<Map<Long, Integer>> winCounters = ThreadLocal.withInitial(HashMap::new);
    private final Map<String, Set<String>> provinceCache = new HashMap<>();
    private final ThreadLocal<Long> initialSpins = ThreadLocal.withInitial(() -> 0L);

    @Transactional
    public Optional<Reward> selectReward(Event event, List<Reward> rewards, long remainingSpins,
            Optional<GoldenHour> goldenHour, String customerLocation) {

        if (remainingSpins <= 0 || rewards.isEmpty()) {
            return Optional.empty();
        }

        // Track total spins
        Long totalSpins = initialSpins.get();
        if (totalSpins == 0 || totalSpins < remainingSpins) {
            totalSpins = remainingSpins;
            initialSpins.set(totalSpins);
        }

        // Calculate current phase
        double progress = (totalSpins - remainingSpins) / (double)totalSpins;
        boolean isEarlyPhase = progress <= EARLY_PHASE;
        boolean isEndgame = progress >= ENDGAME_THRESHOLD;

        // Filter and prepare rewards
        List<Reward> validRewards = new ArrayList<>(Math.min(rewards.size(), MAX_REWARDS));
        Map<Long, Integer> currentWins = winCounters.get();
        int totalRemainingRewards = 0;

        for (Reward reward : rewards) {
            int remaining = reward.getRemainingQuantity();
            if (remaining > 0 && isLocationAllowed(reward, customerLocation)) {
                // Early phase: enforce strict limits on wins
                if (isEarlyPhase) {
                    int maxEarlyWins = reward.getQuantity() / 2; // Half of total quantity
                    int currentWinCount = currentWins.getOrDefault(reward.getId(), 0);
                    if (currentWinCount >= maxEarlyWins) {
                        continue;
                    }
                }
                validRewards.add(reward);
                totalRemainingRewards += remaining;
            }
        }

        if (validRewards.isEmpty() || totalRemainingRewards == 0) {
            return Optional.empty();
        }

        // Must give out all remaining rewards if running out of spins
        if (remainingSpins <= totalRemainingRewards) {
            return selectRewardProportional(validRewards);
        }

        // Endgame phase - Aggressive distribution
        if (isEndgame) {
            double endgameProgress = (progress - ENDGAME_THRESHOLD) / (1.0 - ENDGAME_THRESHOLD);
            double winChance = Math.min(1.0, endgameProgress + 0.3);
            if (ThreadLocalRandom.current().nextDouble() < winChance) {
                return selectRewardProportional(validRewards);
            }
            return Optional.empty();
        }

        // Normal phase - Maintain proper ratios
        double targetRate = totalRemainingRewards / (double)totalSpins;
        if (!isEarlyPhase) {
            // Increase win rate after early phase
            targetRate *= (1.0 + (progress - EARLY_PHASE));
        }

        if (ThreadLocalRandom.current().nextDouble() < targetRate) {
            return selectRewardProportional(validRewards);
        }

        return Optional.empty();
    }

    private Optional<Reward> selectRewardProportional(List<Reward> validRewards) {
        int totalRemaining = validRewards.stream()
                .mapToInt(Reward::getRemainingQuantity)
                .sum();

        int selection = ThreadLocalRandom.current().nextInt(totalRemaining);
        int cumulative = 0;
        
        for (Reward reward : validRewards) {
            cumulative += reward.getRemainingQuantity();
            if (selection < cumulative) {
                return claimReward(reward);
            }
        }
        
        return Optional.empty();
    }

    private Optional<Reward> claimReward(Reward reward) {
        AtomicInteger quantity = getQuantityRef(reward);
        int current;
        do {
            current = quantity.get();
            if (current <= 0) return Optional.empty();
        } while (!quantity.compareAndSet(current, current - 1));

        reward.setRemainingQuantity(current - 1);
        winCounters.get().merge(reward.getId(), 1, Integer::sum);
        return Optional.of(reward);
    }

    private boolean isLocationAllowed(Reward reward, String customerLocation) {
        String provinces = reward.getApplicableProvinces();
        
        if (provinces == null || provinces.isEmpty()) {
            return true;
        }

        if (customerLocation == null || customerLocation.isEmpty()) {
            return false;
        }

        Set<String> locationSet = provinceCache.get(provinces);
        if (locationSet == null) {
            locationSet = new HashSet<>();
            for (String location : provinces.split(",")) {
                locationSet.add(location.trim().toUpperCase());
            }
            provinceCache.put(provinces, locationSet);
        }

        return locationSet.contains(customerLocation.trim().toUpperCase());
    }

    private AtomicInteger getQuantityRef(Reward reward) {
        AtomicInteger[] refs = quantityRefs.get();
        int index = (int)(reward.getId() % refs.length);
        AtomicInteger ref = refs[index];
        if (ref == null) {
            ref = new AtomicInteger(reward.getRemainingQuantity());
            refs[index] = ref;
        } else {
            ref.set(reward.getRemainingQuantity());
        }
        return ref;
    }

    public void resetCaches() {
        provinceCache.clear();
        quantityRefs.remove();
        winCounters.remove();
        initialSpins.remove();
    }
}
