package vn.com.fecredit.app.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.GoldenHour;
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.RewardRepository;

@Service
@RequiredArgsConstructor
public class RewardSelectionService {

    private final RewardRepository rewardRepository;
    private final EventRepository eventRepository;
    private final Random random = new Random();

    @Transactional
    public Optional<Reward> selectReward(Event event, List<Reward> availableRewards, 
                                      long spinId, Optional<GoldenHour> activeGoldenHour, 
                                      String province) {
        if (!event.isActive()) {
            return Optional.empty();
        }

        List<Reward> eligibleRewards;
        if (activeGoldenHour.isPresent()) {
            eligibleRewards = new ArrayList<>();
            eligibleRewards.add(activeGoldenHour.get().getReward());
        } else {
            eligibleRewards = filterEligibleRewards(availableRewards, province);
        }

        if (eligibleRewards.isEmpty()) {
            return Optional.empty();
        }

        double totalProbability = eligibleRewards.stream()
            .mapToDouble(Reward::getProbability)
            .sum();

        if (totalProbability <= 0) {
            return Optional.empty();
        }

        synchronized (this) {
            double randomValue = random.nextDouble() * totalProbability;
            double cumulativeProbability = 0;

            for (Reward reward : eligibleRewards) {
                cumulativeProbability += reward.getProbability();
                if (randomValue <= cumulativeProbability) {
                    if (reward.getRemainingQuantity() > 0) {
                        reward.decrementRemainingQuantity();
                        rewardRepository.save(reward);
                        return Optional.of(reward);
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Transactional
    public Optional<Reward> selectReward(Long eventId, String province) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        List<Reward> availableRewards = rewardRepository.findAvailableRewards(eventId, LocalDateTime.now());
        return selectReward(event, availableRewards, 0L, Optional.empty(), province);
    }

    private List<Reward> filterEligibleRewards(List<Reward> rewards, String province) {
        List<Reward> eligibleRewards = new ArrayList<>();
        for (Reward reward : rewards) {
            if (reward.isAvailable(LocalDateTime.now(), province)) {
                eligibleRewards.add(reward);
            }
        }
        return eligibleRewards;
    }
}
