package vn.com.fecredit.app.util;

import vn.com.fecredit.app.entity.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Value;

/**
 * Extended statistics and analysis methods for events
 */
public class EventStatisticsExtended {

    @Value
    public static class RewardStats {
        String rewardName;
        int totalWins;
        double winRate;
        double avgTimeGap; // Average time between wins in minutes
        List<Integer> peakHours;
        Map<DayOfWeek, Integer> distributionByDay;
    }

    @Value
    public static class ParticipantStats {
        String participantId;
        int totalSpins;
        int totalWins;
        double winRate;
        int uniqueLocationsVisited;
        List<String> favoriteLocations; // Top 3 most visited
        List<Integer> activeHours; // Hours with most activity
    }

    @Value
    public static class LocationStats {
        String locationName;
        int totalSpins;
        int uniqueParticipants;
        double winRate;
        double effectiveMultiplier;
        Map<Integer, Double> hourlyActivity; // Hour -> Activity level (0-1)
        List<String> topRewards; // Most frequently won rewards
    }

    public static Map<String, RewardStats> analyzeRewards(Event event) {
        EventStatisticsValidator.validateEvent(event);
        
        if (event.getSpinHistories() == null || event.getSpinHistories().isEmpty()) {
            return Collections.emptyMap();
        }

        return event.getRewards().stream()
            .collect(Collectors.toMap(
                Reward::getName,
                reward -> calculateRewardStats(reward, event.getSpinHistories())
            ));
    }

    private static RewardStats calculateRewardStats(Reward reward, List<SpinHistory> spins) {
        List<SpinHistory> rewardWins = spins.stream()
            .filter(spin -> spin.isWin() && Objects.equals(spin.getReward(), reward))
            .sorted(Comparator.comparing(SpinHistory::getSpinTime))
            .collect(Collectors.toList());

        double winRate = (double) rewardWins.size() / spins.size();
        double avgTimeGap = calculateAverageTimeGap(rewardWins);
        List<Integer> peakHours = findPeakHoursForReward(rewardWins);
        Map<DayOfWeek, Integer> dayDistribution = calculateDayDistribution(rewardWins);

        return new RewardStats(
            reward.getName(),
            rewardWins.size(),
            winRate,
            avgTimeGap,
            peakHours,
            dayDistribution
        );
    }

    private static double calculateAverageTimeGap(List<SpinHistory> sortedWins) {
        if (sortedWins.size() < 2) return 0.0;

        double totalGap = 0.0;
        for (int i = 1; i < sortedWins.size(); i++) {
            Duration gap = Duration.between(
                sortedWins.get(i-1).getSpinTime(),
                sortedWins.get(i).getSpinTime()
            );
            totalGap += gap.toMinutes();
        }
        return totalGap / (sortedWins.size() - 1);
    }

    private static List<Integer> findPeakHoursForReward(List<SpinHistory> wins) {
        if (wins.isEmpty()) return Collections.emptyList();

        Map<Integer, Long> hourCounts = wins.stream()
            .collect(Collectors.groupingBy(
                spin -> spin.getSpinTime().getHour(),
                Collectors.counting()
            ));

        long maxCount = Collections.max(hourCounts.values());
        double threshold = maxCount * 0.8;

        return hourCounts.entrySet().stream()
            .filter(e -> e.getValue() >= threshold)
            .map(Map.Entry::getKey)
            .sorted()
            .collect(Collectors.toList());
    }

    private static Map<DayOfWeek, Integer> calculateDayDistribution(List<SpinHistory> spins) {
        return spins.stream()
            .collect(Collectors.groupingBy(
                spin -> spin.getSpinTime().getDayOfWeek(),
                Collectors.collectingAndThen(
                    Collectors.counting(),
                    Long::intValue
                )
            ));
    }

    public static Map<String, ParticipantStats> analyzeParticipants(Event event) {
        EventStatisticsValidator.validateEvent(event);

        if (event.getParticipants() == null || event.getParticipants().isEmpty()) {
            return Collections.emptyMap();
        }

        return event.getParticipants().stream()
            .collect(Collectors.toMap(
                Participant::getCustomerId,
                participant -> calculateParticipantStats(participant, event)
            ));
    }

    private static ParticipantStats calculateParticipantStats(Participant participant, Event event) {
        List<SpinHistory> participantSpins = event.getSpinHistories().stream()
            .filter(spin -> Objects.equals(spin.getParticipant(), participant))
            .collect(Collectors.toList());

        int totalSpins = participantSpins.size();
        int totalWins = (int) participantSpins.stream().filter(SpinHistory::isWin).count();
        double winRate = totalSpins > 0 ? (double) totalWins / totalSpins : 0.0;

        Set<EventLocation> visitedLocations = participantSpins.stream()
            .map(SpinHistory::getEventLocation)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        List<String> favoriteLocations = findFavoriteLocations(participantSpins);
        List<Integer> activeHours = findActiveHours(participantSpins);

        return new ParticipantStats(
            participant.getCustomerId(),
            totalSpins,
            totalWins,
            winRate,
            visitedLocations.size(),
            favoriteLocations,
            activeHours
        );
    }

    private static List<String> findFavoriteLocations(List<SpinHistory> spins) {
        return spins.stream()
            .map(SpinHistory::getEventLocation)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                EventLocation::getName,
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private static List<Integer> findActiveHours(List<SpinHistory> spins) {
        return spins.stream()
            .collect(Collectors.groupingBy(
                spin -> spin.getSpinTime().getHour(),
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .sorted()
            .collect(Collectors.toList());
    }

    public static Map<String, LocationStats> analyzeLocations(Event event) {
        EventStatisticsValidator.validateEvent(event);

        if (event.getLocations() == null || event.getLocations().isEmpty()) {
            return Collections.emptyMap();
        }

        return event.getLocations().stream()
            .collect(Collectors.toMap(
                EventLocation::getName,
                location -> calculateLocationStats(location, event)
            ));
    }

    private static LocationStats calculateLocationStats(EventLocation location, Event event) {
        List<SpinHistory> locationSpins = location.getSpinHistories();

        int totalSpins = locationSpins.size();
        Set<Participant> uniqueParticipants = locationSpins.stream()
            .map(SpinHistory::getParticipant)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        double winRate = locationSpins.stream()
            .filter(SpinHistory::isWin)
            .count() / (double) totalSpins;

        Map<Integer, Double> hourlyActivity = calculateHourlyActivity(locationSpins);
        List<String> topRewards = findTopRewards(locationSpins);

        return new LocationStats(
            location.getName(),
            totalSpins,
            uniqueParticipants.size(),
            winRate,
            location.getWinProbabilityMultiplier(),
            hourlyActivity,
            topRewards
        );
    }

    private static Map<Integer, Double> calculateHourlyActivity(List<SpinHistory> spins) {
        long totalSpins = spins.size();
        if (totalSpins == 0) return Collections.emptyMap();

        Map<Integer, Long> hourCounts = spins.stream()
            .collect(Collectors.groupingBy(
                spin -> spin.getSpinTime().getHour(),
                Collectors.counting()
            ));

        return hourCounts.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue() / (double) totalSpins
            ));
    }

    private static List<String> findTopRewards(List<SpinHistory> spins) {
        return spins.stream()
            .filter(SpinHistory::isWin)
            .filter(spin -> spin.getReward() != null)
            .collect(Collectors.groupingBy(
                spin -> spin.getReward().getName(),
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
