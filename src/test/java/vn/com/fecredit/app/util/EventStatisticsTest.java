package vn.com.fecredit.app.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.*;
import java.util.*;
import vn.com.fecredit.app.entity.*;

class EventStatisticsTest {

    private Event event;
    private Reward reward1;
    private Reward reward2;
    private EventLocation location1;
    private EventLocation location2;

    @BeforeEach
    void setUp() {
        TestDataBuilder.resetCounter();
        event = TestDataBuilder.anEvent()
            .name("Test Event")
            .startDate(LocalDateTime.now().minusDays(1))
            .endDate(LocalDateTime.now().plusDays(6))
            .isActive(true)
            .build();

        reward1 = TestDataBuilder.aReward()
            .name("High Value Reward")
            .winProbability(0.1)
            .quantity(100)
            .dailyLimit(10)
            .build();

        reward2 = TestDataBuilder.aReward()
            .name("Low Value Reward")
            .winProbability(0.5)
            .quantity(1000)
            .dailyLimit(100)
            .build();

        location1 = TestDataBuilder.anEventLocation()
            .name("Location 1")
            .winProbabilityMultiplier(1.5)
            .build();

        location2 = TestDataBuilder.anEventLocation()
            .name("Location 2")
            .winProbabilityMultiplier(2.0)
            .build();

        event.addReward(reward1);
        event.addReward(reward2);
        event.addLocation(location1);
        event.addLocation(location2);

        TestEntitySetter.setRewardEvent(reward1, event);
        TestEntitySetter.setRewardEvent(reward2, event);
        TestEntitySetter.setLocationEvent(location1, event);
        TestEntitySetter.setLocationEvent(location2, event);

        // Add spins with known outcomes for testing
        addTestSpins();
    }

    private void addTestSpins() {
        LocalDateTime baseTime = event.getStartDate();
        
        // Add winning spins at 9 AM
        for (int i = 0; i < 8; i++) {
            SpinHistory spin = TestDataBuilder.aSpinHistory()
                .spinTime(baseTime.plusHours(9))
                .isWin(true)
                .build();
            TestEntitySetter.setSpinEvent(spin, event);
            TestEntitySetter.setSpinLocation(spin, location1);
            TestEntitySetter.setSpinReward(spin, reward1);
            event.addSpinHistory(spin);
            location1.addSpinHistory(spin);
        }

        // Add losing spins at 9 AM
        for (int i = 0; i < 2; i++) {
            SpinHistory spin = TestDataBuilder.aSpinHistory()
                .spinTime(baseTime.plusHours(9))
                .isWin(false)
                .build();
            TestEntitySetter.setSpinEvent(spin, event);
            TestEntitySetter.setSpinLocation(spin, location1);
            event.addSpinHistory(spin);
            location1.addSpinHistory(spin);
        }

        // Add winning spins at 2 PM
        for (int i = 0; i < 3; i++) {
            SpinHistory spin = TestDataBuilder.aSpinHistory()
                .spinTime(baseTime.plusHours(14))
                .isWin(true)
                .build();
            TestEntitySetter.setSpinEvent(spin, event);
            TestEntitySetter.setSpinLocation(spin, location2);
            TestEntitySetter.setSpinReward(spin, reward2);
            event.addSpinHistory(spin);
            location2.addSpinHistory(spin);
        }

        // Add losing spins at 2 PM
        for (int i = 0; i < 7; i++) {
            SpinHistory spin = TestDataBuilder.aSpinHistory()
                .spinTime(baseTime.plusHours(14))
                .isWin(false)
                .build();
            TestEntitySetter.setSpinEvent(spin, event);
            TestEntitySetter.setSpinLocation(spin, location2);
            event.addSpinHistory(spin);
            location2.addSpinHistory(spin);
        }
    }

    @Test
    void analyze_ShouldCalculateCorrectStatistics() {
        EventStatistics.EventAnalysis analysis = EventStatistics.analyze(event);

        // Test hourly win rates
        Map<Integer, Double> hourlyRates = analysis.getHourlyWinRates();
        assertThat(hourlyRates.get(9)).isEqualTo(0.8); // 8 wins out of 10 spins at 9 AM
        assertThat(hourlyRates.get(14)).isEqualTo(0.3); // 3 wins out of 10 spins at 2 PM

        // Test overall win rate
        assertThat(analysis.getOverallWinRate()).isEqualTo(0.55); // 11 wins out of 20 total spins

        // Test peak hours
        assertThat(analysis.getPeakHours()).contains(9); // 9 AM has highest win rate

        // Test reward distribution
        Map<String, Integer> rewardDist = analysis.getRewardDistribution();
        assertThat(rewardDist.get("High Value Reward")).isEqualTo(8);
        assertThat(rewardDist.get("Low Value Reward")).isEqualTo(3);
    }

    @Test
    void calculateLocationEffectiveness_ShouldConsiderMultiplier() {
        double location1Effectiveness = EventStatistics.calculateLocationEffectiveness(location1);
        double location2Effectiveness = EventStatistics.calculateLocationEffectiveness(location2);

        // Location 1: 80% win rate * 1.5 multiplier = 1.2
        assertThat(location1Effectiveness).isEqualTo(1.2);

        // Location 2: 30% win rate * 2.0 multiplier = 0.6
        assertThat(location2Effectiveness).isEqualTo(0.6);
    }

    @Test
    void calculateLocationWinRates_ShouldReturnCorrectRates() {
        Map<String, Double> locationRates = EventStatistics.calculateLocationWinRates(event);

        assertThat(locationRates.get("Location 1")).isEqualTo(0.8); // 8 wins out of 10
        assertThat(locationRates.get("Location 2")).isEqualTo(0.3); // 3 wins out of 10
    }

    @Test
    void analyzeHourlyActivity_ShouldReturnCorrectActivityData() {
        List<EventStatistics.HourlyActivity> activities = EventStatistics.analyzeHourlyActivity(event);

        // Verify 9 AM activity
        Optional<EventStatistics.HourlyActivity> morning = activities.stream()
            .filter(a -> a.getHour() == 9)
            .findFirst();
        assertThat(morning).isPresent();
        assertThat(morning.get().getSpinCount()).isEqualTo(10);
        assertThat(morning.get().getWinRate()).isEqualTo(0.8);

        // Verify 2 PM activity
        Optional<EventStatistics.HourlyActivity> afternoon = activities.stream()
            .filter(a -> a.getHour() == 14)
            .findFirst();
        assertThat(afternoon).isPresent();
        assertThat(afternoon.get().getSpinCount()).isEqualTo(10);
        assertThat(afternoon.get().getWinRate()).isEqualTo(0.3);
    }

    @Test
    void calculateDailyWinRates_ShouldGroupByDayOfWeek() {
        Map<DayOfWeek, Double> dailyRates = EventStatistics.calculateDailyWinRates(event);

        DayOfWeek spinDay = event.getStartDate().getDayOfWeek();
        assertThat(dailyRates).containsKey(spinDay);
        assertThat(dailyRates.get(spinDay)).isEqualTo(0.55); // Overall win rate for that day
    }
}
