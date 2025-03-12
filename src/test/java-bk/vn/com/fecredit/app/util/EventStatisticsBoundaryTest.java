package vn.com.fecredit.app.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.*;
import java.util.*;
import vn.com.fecredit.app.entity.*;

class EventStatisticsBoundaryTest {
    private Event event;
    private EventLocation location;
    private Reward reward;

    @BeforeEach
    void setUp() {
        TestDataBuilder.resetCounter();
        event = TestDataBuilder.anEvent()
            .name("Boundary Test Event")
            .startDate(LocalDateTime.now().minusDays(1))
            .endDate(LocalDateTime.now().plusDays(6))
            .isActive(true)
            .build();

        location = TestDataBuilder.anEventLocation()
            .name("Test Location")
            .winProbabilityMultiplier(1.5)
            .build();

        reward = TestDataBuilder.aReward()
            .name("Test Reward")
            .winProbability(0.1)
            .quantity(100)
            .dailyLimit(10)
            .build();

        event.addLocation(location);
        event.addReward(reward);
        TestEntitySetter.setLocationEvent(location, event);
        TestEntitySetter.setRewardEvent(reward, event);
    }

    @Test
    void analyze_ShouldHandleEmptyEvent() {
        EventStatistics.EventAnalysis analysis = EventStatistics.analyze(event);

        assertThat(analysis.getHourlyWinRates()).isEmpty();
        assertThat(analysis.getOverallWinRate()).isZero();
        assertThat(analysis.getAverageRewardValue()).isZero();
        assertThat(analysis.getPeakHours()).isEmpty();
        assertThat(analysis.getRewardDistribution()).isEmpty();
    }

    @Test
    void analyze_ShouldHandleSingleSpin() {
        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(LocalDateTime.now())
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(spin, event);
        TestEntitySetter.setSpinLocation(spin, location);
        TestEntitySetter.setSpinReward(spin, reward);
        event.addSpinHistory(spin);
        location.addSpinHistory(spin);

        EventStatistics.EventAnalysis analysis = EventStatistics.analyze(event);
        assertThat(analysis.getOverallWinRate()).isEqualTo(1.0);
        assertThat(analysis.getHourlyWinRates()).hasSize(1);
        assertThat(analysis.getRewardDistribution()).hasSize(1);
    }

    @Test
    void calculateLocationEffectiveness_ShouldHandleEmptyLocation() {
        EventLocation emptyLocation = TestDataBuilder.anEventLocation()
            .name("Empty Location")
            .winProbabilityMultiplier(1.5)
            .build();
        TestEntitySetter.setLocationEvent(emptyLocation, event);

        double effectiveness = EventStatistics.calculateLocationEffectiveness(emptyLocation);
        assertThat(effectiveness).isZero();
    }

    @Test
    void analyzeHourlyActivity_ShouldHandleAllLosingSpins() {
        // Add 24 losing spins, one for each hour
        LocalDateTime baseTime = event.getStartDate();
        for (int hour = 0; hour < 24; hour++) {
            SpinHistory spin = TestDataBuilder.aSpinHistory()
                .spinTime(baseTime.plusHours(hour))
                .isWin(false)
                .build();
            TestEntitySetter.setSpinEvent(spin, event);
            TestEntitySetter.setSpinLocation(spin, location);
            event.addSpinHistory(spin);
            location.addSpinHistory(spin);
        }

        List<EventStatistics.HourlyActivity> activities = EventStatistics.analyzeHourlyActivity(event);
        assertThat(activities).hasSize(24);
        activities.forEach(activity -> assertThat(activity.getWinRate()).isZero());
    }

    @Test
    void analyzeHourlyActivity_ShouldHandleAllWinningSpins() {
        // Add 24 winning spins, one for each hour
        LocalDateTime baseTime = event.getStartDate();
        for (int hour = 0; hour < 24; hour++) {
            SpinHistory spin = TestDataBuilder.aSpinHistory()
                .spinTime(baseTime.plusHours(hour))
                .isWin(true)
                .build();
            TestEntitySetter.setSpinEvent(spin, event);
            TestEntitySetter.setSpinLocation(spin, location);
            TestEntitySetter.setSpinReward(spin, reward);
            event.addSpinHistory(spin);
            location.addSpinHistory(spin);
        }

        List<EventStatistics.HourlyActivity> activities = EventStatistics.analyzeHourlyActivity(event);
        assertThat(activities).hasSize(24);
        activities.forEach(activity -> assertThat(activity.getWinRate()).isEqualTo(1.0));
    }

    @Test
    void calculateDailyWinRates_ShouldHandleSingleDayEvent() {
        LocalDateTime now = LocalDateTime.now();
        event.setStartDate(now);
        event.setEndDate(now.plusHours(23));

        // Add some spins
        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(now.plusHours(1))
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(spin, event);
        event.addSpinHistory(spin);

        Map<DayOfWeek, Double> rates = EventStatistics.calculateDailyWinRates(event);
        assertThat(rates).hasSize(1);
        assertThat(rates.get(now.getDayOfWeek())).isEqualTo(1.0);
    }

    @Test
    void calculateLocationWinRates_ShouldHandleMaxProbabilityMultiplier() {
        location.setWinProbabilityMultiplier(Double.MAX_VALUE);

        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(LocalDateTime.now())
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(spin, event);
        TestEntitySetter.setSpinLocation(spin, location);
        event.addSpinHistory(spin);
        location.addSpinHistory(spin);

        Map<String, Double> rates = EventStatistics.calculateLocationWinRates(event);
        assertThat(rates.get(location.getName())).isEqualTo(1.0);
    }

    @Test
    void calculateLocationEffectiveness_ShouldHandleExtremeMultipliers() {
        location.setWinProbabilityMultiplier(Double.MAX_VALUE);
        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(LocalDateTime.now())
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(spin, event);
        TestEntitySetter.setSpinLocation(spin, location);
        event.addSpinHistory(spin);
        location.addSpinHistory(spin);

        double effectiveness = EventStatistics.calculateLocationEffectiveness(location);
        assertThat(effectiveness).isFinite();
        assertThat(effectiveness).isGreaterThan(0.0);
    }

    @Test
    void analyze_ShouldHandleSpinsAtMidnight() {
        LocalDateTime midnight = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(midnight)
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(spin, event);
        event.addSpinHistory(spin);

        EventStatistics.EventAnalysis analysis = EventStatistics.analyze(event);
        assertThat(analysis.getHourlyWinRates()).containsKey(0);
        assertThat(analysis.getHourlyWinRates().get(0)).isEqualTo(1.0);
    }
}
