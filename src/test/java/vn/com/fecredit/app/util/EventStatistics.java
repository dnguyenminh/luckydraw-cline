package vn.com.fecredit.app.util;

import vn.com.fecredit.app.entity.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for analyzing event statistics and metrics
 */
public class EventStatistics {
    
    public static class EventAnalysis {
        private final Map<Integer, Double> hourlyWinRates;
        private final double overallWinRate;
        private final double averageRewardValue;
        private final List<Integer> peakHours;
        private final Map<String, Integer> rewardDistribution;

        public EventAnalysis(
            Map<Integer, Double> hourlyWinRates,
            double overallWinRate,
            double averageRewardValue,
            List<Integer> peakHours,
            Map<String, Integer> rewardDistribution
        ) {
            this.hourlyWinRates = new HashMap<>(hourlyWinRates);
            this.overallWinRate = overallWinRate;
            this.averageRewardValue = averageRewardValue;
            this.peakHours = new ArrayList<>(peakHours);
            this.rewardDistribution = new HashMap<>(rewardDistribution);
        }

        public Map<Integer, Double> getHourlyWinRates() {
            return Collections.unmodifiableMap(hourlyWinRates);
        }

        public double getOverallWinRate() {
            return overallWinRate;
        }

        public double getAverageRewardValue() {
            return averageRewardValue;
        }

        public List<Integer> getPeakHours() {
            return Collections.unmodifiableList(peakHours);
        }

        public Map<String, Integer> getRewardDistribution() {
            return Collections.unmodifiableMap(rewardDistribution);
        }
    }

    public static EventAnalysis analyze(Event event) {
        EventStatisticsValidator.validateEvent(event);
        
        List<SpinHistory> spins = event.getSpinHistories();
        if (spins == null) spins = Collections.emptyList();
        
        EventStatisticsValidator.validateSpinHistories(spins);
        EventStatisticsValidator.validateAnalysisInputs(event, spins);
        
        Map<Integer, Double> hourlyWinRates = calculateHourlyWinRates(spins);
        double overallWinRate = calculateWinRate(spins);
        double avgRewardValue = calculateAverageRewardValue(spins);
        List<Integer> peakHours = findPeakHours(hourlyWinRates);
        Map<String, Integer> rewardDist = calculateRewardDistribution(spins);

        return new EventAnalysis(
            hourlyWinRates,
            overallWinRate,
            avgRewardValue,
            peakHours,
            rewardDist
        );
    }

    private static Map<Integer, Double> calculateHourlyWinRates(List<SpinHistory> spins) {
        return spins.stream()
            .filter(spin -> spin.getSpinTime() != null)
            .collect(Collectors.groupingBy(
                spin -> spin.getSpinTime().getHour(),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    hourSpins -> calculateWinRate(hourSpins)
                )
            ));
    }

    private static double calculateWinRate(List<SpinHistory> spins) {
        if (spins == null || spins.isEmpty()) return 0.0;
        long winCount = spins.stream()
            .filter(SpinHistory::isWin)
            .count();
        return (double) winCount / spins.size();
    }

    private static double calculateAverageRewardValue(List<SpinHistory> spins) {
        if (spins == null) return 0.0;
        return spins.stream()
            .filter(SpinHistory::isWin)
            .filter(spin -> spin.getReward() != null)
            .mapToDouble(spin -> spin.getReward().getWinProbability())
            .average()
            .orElse(0.0);
    }

    private static List<Integer> findPeakHours(Map<Integer, Double> hourlyWinRates) {
        if (hourlyWinRates.isEmpty()) return Collections.emptyList();
        
        double maxRate = Collections.max(hourlyWinRates.values());
        double threshold = maxRate * 0.8; // Consider hours with 80%+ of max rate as peak

        return hourlyWinRates.entrySet().stream()
            .filter(e -> e.getValue() >= threshold)
            .map(Map.Entry::getKey)
            .sorted()
            .collect(Collectors.toList());
    }

    private static Map<String, Integer> calculateRewardDistribution(List<SpinHistory> spins) {
        if (spins == null) return Collections.emptyMap();
        return spins.stream()
            .filter(SpinHistory::isWin)
            .filter(spin -> spin.getReward() != null)
            .collect(Collectors.groupingBy(
                spin -> spin.getReward().getName(),
                Collectors.collectingAndThen(
                    Collectors.counting(),
                    Long::intValue
                )
            ));
    }

    public static double calculateLocationEffectiveness(EventLocation location) {
        EventStatisticsValidator.validateLocation(location);
        
        if (location.getSpinHistories() == null || location.getSpinHistories().isEmpty()) {
            return 0.0;
        }

        double baseWinRate = calculateWinRate(location.getSpinHistories());
        double multiplier = location.getWinProbabilityMultiplier();
        EventStatisticsValidator.validateLocationMultiplier(multiplier);
        
        return baseWinRate * multiplier;
    }

    public static Map<DayOfWeek, Double> calculateDailyWinRates(Event event) {
        EventStatisticsValidator.validateEvent(event);
        
        if (event.getSpinHistories() == null) {
            return Collections.emptyMap();
        }

        return event.getSpinHistories().stream()
            .filter(spin -> spin.getSpinTime() != null)
            .collect(Collectors.groupingBy(
                spin -> spin.getSpinTime().getDayOfWeek(),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    EventStatistics::calculateWinRate
                )
            ));
    }

    public static Map<String, Double> calculateLocationWinRates(Event event) {
        EventStatisticsValidator.validateEvent(event);
        
        if (event.getLocations() == null) {
            return Collections.emptyMap();
        }

        return event.getLocations().stream()
            .collect(Collectors.toMap(
                EventLocation::getName,
                location -> calculateWinRate(location.getSpinHistories())
            ));
    }

    public static class HourlyActivity {
        private final int hour;
        private final int spinCount;
        private final double winRate;
        private final boolean isGoldenHour;

        public HourlyActivity(int hour, int spinCount, double winRate, boolean isGoldenHour) {
            if (hour < 0 || hour > 23) throw new IllegalArgumentException("Invalid hour value");
            if (spinCount < 0) throw new IllegalArgumentException("Spin count cannot be negative");
            if (winRate < 0 || winRate > 1) throw new IllegalArgumentException("Win rate must be between 0 and 1");
            
            this.hour = hour;
            this.spinCount = spinCount;
            this.winRate = winRate;
            this.isGoldenHour = isGoldenHour;
        }

        public int getHour() { return hour; }
        public int getSpinCount() { return spinCount; }
        public double getWinRate() { return winRate; }
        public boolean isGoldenHour() { return isGoldenHour; }
    }

    public static List<HourlyActivity> analyzeHourlyActivity(Event event) {
        EventStatisticsValidator.validateEvent(event);
        
        if (event.getSpinHistories() == null) {
            return Collections.emptyList();
        }

        Map<Integer, List<SpinHistory>> hourlySpins = event.getSpinHistories().stream()
            .filter(spin -> spin.getSpinTime() != null)
            .collect(Collectors.groupingBy(spin -> spin.getSpinTime().getHour()));

        Set<Integer> goldenHours = event.getGoldenHours().stream()
            .filter(gh -> gh.getStartTime() != null)
            .map(gh -> gh.getStartTime().getHour())
            .collect(Collectors.toSet());

        return hourlySpins.entrySet().stream()
            .map(entry -> new HourlyActivity(
                entry.getKey(),
                entry.getValue().size(),
                calculateWinRate(entry.getValue()),
                goldenHours.contains(entry.getKey())
            ))
            .sorted(Comparator.comparingInt(HourlyActivity::getHour))
            .collect(Collectors.toList());
    }
}
