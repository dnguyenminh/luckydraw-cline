package vn.com.fecredit.app.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;

@Component
@RequiredArgsConstructor
public class RewardMapper {

    public Reward createEntityFromRequest(RewardDTO.CreateRewardRequest request) {
        return Reward.builder()
                .eventId(request.getEventId())
                .name(request.getName())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .remainingQuantity(request.getQuantity())
                .probability(request.getProbability())
                .active(request.getActive() != null ? request.getActive() : true)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }

    public void updateEntityFromRequest(Reward reward, RewardDTO.UpdateRewardRequest request) {
        if (request.getName() != null) {
            reward.setName(request.getName());
        }
        if (request.getDescription() != null) {
            reward.setDescription(request.getDescription());
        }
        if (request.getQuantity() != null) {
            reward.updateQuantity(request.getQuantity());
        }
        if (request.getRemainingQuantity() != null) {
            reward.setRemainingQuantity(request.getRemainingQuantity());
        }
        if (request.getProbability() != null) {
            reward.setProbability(request.getProbability());
        }
        if (request.getActive() != null) {
            reward.setActive(request.getActive());
        }
        if (request.getStartDate() != null) {
            reward.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            reward.setEndDate(request.getEndDate());
        }
    }

    public RewardDTO.RewardResponse toResponse(Reward reward) {
        Event event = reward.getEvent();
        return RewardDTO.RewardResponse.builder()
                .id(reward.getId())
                .eventId(event.getId())
                .eventName(event.getName())
                .name(reward.getName())
                .description(reward.getDescription())
                .quantity(reward.getQuantity())
                .remainingQuantity(reward.getRemainingQuantity())
                .probability(reward.getProbability())
                .effectiveProbability(reward.getEffectiveProbability())
                .active(reward.isActive())
                .startDate(reward.getStartDate())
                .endDate(reward.getEndDate())
                .createdAt(reward.getCreatedAt())
                .updatedAt(reward.getUpdatedAt())
                .statistics(calculateStatistics(reward))
                .build();
    }

    public RewardDTO.RewardSummary toSummary(Reward reward) {
        return RewardDTO.RewardSummary.builder()
                .id(reward.getId())
                .name(reward.getName())
                .remainingQuantity(reward.getRemainingQuantity())
                .probability(reward.getProbability())
                .active(reward.isActive())
                .endDate(reward.getEndDate())
                .build();
    }

    public RewardDTO.RewardStatistics calculateStatistics(Reward reward) {
        Set<SpinHistory> spins = reward.getSpinHistories();
        if (spins == null || spins.isEmpty()) {
            return RewardDTO.RewardStatistics.builder()
                    .totalWins(0)
                    .totalSpins(0)
                    .actualWinRate(0.0)
                    .theoreticalWinRate(reward.getProbability())
                    .averageProbabilityMultiplier(1.0)
                    .winsToday(0)
                    .winsThisWeek(0)
                    .winsThisMonth(0)
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        long totalSpins = spins.size();
        long totalWins = spins.stream().filter(SpinHistory::isWin).count();
        double actualWinRate = (double) totalWins / totalSpins;

        double avgMultiplier = spins.stream()
                .filter(s -> s.getProbabilityMultiplier() != null)
                .mapToDouble(SpinHistory::getProbabilityMultiplier)
                .average()
                .orElse(1.0);

        LocalDateTime lastWinDate = spins.stream()
                .filter(SpinHistory::isWin)
                .map(SpinHistory::getSpinDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        long winsToday = spins.stream()
                .filter(s -> s.isWin() && s.getSpinDate().toLocalDate().equals(now.toLocalDate()))
                .count();

        long winsThisWeek = spins.stream()
                .filter(s -> s.isWin() && s.getSpinDate().isAfter(now.minusWeeks(1)))
                .count();

        long winsThisMonth = spins.stream()
                .filter(s -> s.isWin() && s.getSpinDate().isAfter(now.minusMonths(1)))
                .count();

        Long remainingDays = reward.getEndDate() != null ? 
                now.until(reward.getEndDate(), java.time.temporal.ChronoUnit.DAYS) : null;

        return RewardDTO.RewardStatistics.builder()
                .totalWins((int) totalWins)
                .totalSpins((int) totalSpins)
                .actualWinRate(actualWinRate)
                .theoreticalWinRate(reward.getProbability())
                .averageProbabilityMultiplier(avgMultiplier)
                .winsToday((int) winsToday)
                .winsThisWeek((int) winsThisWeek)
                .winsThisMonth((int) winsThisMonth)
                .lastWinDate(lastWinDate)
                .estimatedRemainingDays(remainingDays)
                .build();
    }

    public List<RewardDTO.RewardResponse> toResponseList(List<Reward> rewards) {
        return rewards.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<RewardDTO.RewardSummary> toSummaryList(List<Reward> rewards) {
        return rewards.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }
}
