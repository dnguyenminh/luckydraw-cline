package vn.com.fecredit.app.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.SpinHistory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class EventStatistics {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventAnalysis {
        private long totalSpins;
        private long totalWins;
        private double winRate;
        private Map<String, Double> locationWinRates;
        private Map<Integer, Long> hourlyActivity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyActivity {
        private int hour;
        private long spins;
        private long wins;
        private double winRate;
    }

    public static EventAnalysis analyze(Event event) {
        if (event == null) return null;
        
        List<SpinHistory> spins = new ArrayList<>(event.getSpinHistories());
        return EventAnalysis.builder()
            .totalSpins(spins.size())
            .totalWins(calculateWins(spins))
            .winRate(calculateWinRate(spins))
            .locationWinRates(calculateLocationWinRates(event))
            .hourlyActivity(analyzeHourlyActivity(event).stream()
                .collect(Collectors.toMap(
                    HourlyActivity::getHour,
                    HourlyActivity::getSpins)))
            .build();
    }

    public static Map<String, Double> calculateLocationWinRates(Event event) {
        if (event == null) return Collections.emptyMap();
        
        return event.getLocations().stream()
            .collect(Collectors.toMap(
                EventLocation::getName,
                location -> calculateLocationEffectiveness(location)
            ));
    }

    public static double calculateLocationEffectiveness(EventLocation location) {
        if (location == null || location.getSpinHistories() == null) return 0.0;
        return calculateWinRate(new ArrayList<>(location.getSpinHistories()));
    }

    public static Map<Integer, Long> calculateDailyWinRates(Event event) {
        if (event == null) return Collections.emptyMap();
        
        return event.getSpinHistories().stream()
            .filter(spin -> spin.getSpinTime() != null)
            .collect(Collectors.groupingBy(
                spin -> spin.getSpinTime().getDayOfMonth(),
                Collectors.counting()
            ));
    }

    public static List<HourlyActivity> analyzeHourlyActivity(Event event) {
        if (event == null) return Collections.emptyList();
        
        Map<Integer, List<SpinHistory>> hourlySpins = event.getSpinHistories().stream()
            .filter(spin -> spin.getSpinTime() != null)
            .collect(Collectors.groupingBy(
                spin -> spin.getSpinTime().getHour()
            ));

        return hourlySpins.entrySet().stream()
            .map(entry -> HourlyActivity.builder()
                .hour(entry.getKey())
                .spins(entry.getValue().size())
                .wins(calculateWins(entry.getValue()))
                .winRate(calculateWinRate(entry.getValue()))
                .build())
            .collect(Collectors.toList());
    }

    private static long calculateWins(List<SpinHistory> spins) {
        return spins.stream()
            .filter(SpinHistory::isWin)
            .count();
    }

    private static double calculateWinRate(List<SpinHistory> spins) {
        if (spins == null || spins.isEmpty()) return 0.0;
        return (double) calculateWins(spins) / spins.size();
    }
}
