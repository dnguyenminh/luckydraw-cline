package vn.com.fecredit.app.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.*;
import java.util.*;
import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.exception.BusinessException;

class EventStatisticsErrorTest {
    private Event event;
    private EventLocation location;
    private Reward reward;

    @BeforeEach
    void setUp() {
        TestDataBuilder.resetCounter();
        event = TestDataBuilder.anEvent()
            .name("Error Test Event")
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
    }

    @Test
    void analyze_ShouldHandleNullEvent() {
        assertThatThrownBy(() -> EventStatistics.analyze(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Event cannot be null");
    }

    @Test
    void analyze_ShouldHandleNullSpinHistory() {
        event.setSpinHistories(null);
        
        EventStatistics.EventAnalysis analysis = EventStatistics.analyze(event);
        assertThat(analysis.getHourlyWinRates()).isEmpty();
        assertThat(analysis.getOverallWinRate()).isZero();
    }

    @Test
    void calculateLocationEffectiveness_ShouldHandleNullLocation() {
        assertThatThrownBy(() -> EventStatistics.calculateLocationEffectiveness(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Location cannot be null");
    }

    @Test
    void calculateLocationEffectiveness_ShouldHandleNullSpinHistory() {
        location.setSpinHistories(null);
        
        double effectiveness = EventStatistics.calculateLocationEffectiveness(location);
        assertThat(effectiveness).isZero();
    }

    @Test
    void calculateDailyWinRates_ShouldHandleNullEvent() {
        assertThatThrownBy(() -> EventStatistics.calculateDailyWinRates(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Event cannot be null");
    }

    @Test
    void analyzeHourlyActivity_ShouldHandleNullEvent() {
        assertThatThrownBy(() -> EventStatistics.analyzeHourlyActivity(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Event cannot be null");
    }

    @Test
    void calculateLocationWinRates_ShouldHandleNullEvent() {
        assertThatThrownBy(() -> EventStatistics.calculateLocationWinRates(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Event cannot be null");
    }

    @Test
    void analyze_ShouldHandleInvalidSpinTimes() {
        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(null)
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(spin, event);
        event.addSpinHistory(spin);

        assertDoesNotThrow(() -> EventStatistics.analyze(event));
        EventStatistics.EventAnalysis analysis = EventStatistics.analyze(event);
        assertThat(analysis.getHourlyWinRates()).isEmpty();
    }

    @Test
    void analyze_ShouldHandleCorruptedRewardReferences() {
        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(LocalDateTime.now())
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(spin, event);
        event.addSpinHistory(spin);
        // Don't set reward reference even though spin is marked as win

        EventStatistics.EventAnalysis analysis = EventStatistics.analyze(event);
        assertThat(analysis.getRewardDistribution()).isEmpty();
    }

    @Test
    void calculateLocationEffectiveness_ShouldHandleZeroMultiplier() {
        location.setWinProbabilityMultiplier(0.0);
        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(LocalDateTime.now())
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(spin, event);
        TestEntitySetter.setSpinLocation(spin, location);
        location.addSpinHistory(spin);

        double effectiveness = EventStatistics.calculateLocationEffectiveness(location);
        assertThat(effectiveness).isZero();
    }

    @Test
    void calculateLocationEffectiveness_ShouldHandleNegativeMultiplier() {
        location.setWinProbabilityMultiplier(-1.5);
        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(LocalDateTime.now())
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(spin, event);
        TestEntitySetter.setSpinLocation(spin, location);
        location.addSpinHistory(spin);

        assertThatThrownBy(() -> EventStatistics.calculateLocationEffectiveness(location))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Probability multiplier must be positive");
    }

    @Test
    void analyzeHourlyActivity_ShouldHandleInconsistentData() {
        // Add spin with future time
        SpinHistory futureSpin = TestDataBuilder.aSpinHistory()
            .spinTime(LocalDateTime.now().plusYears(1))
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(futureSpin, event);
        event.addSpinHistory(futureSpin);

        // Add spin with past time
        SpinHistory pastSpin = TestDataBuilder.aSpinHistory()
            .spinTime(LocalDateTime.now().minusYears(1))
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(pastSpin, event);
        event.addSpinHistory(pastSpin);

        List<EventStatistics.HourlyActivity> activities = EventStatistics.analyzeHourlyActivity(event);
        assertThat(activities).hasSize(2);
        activities.forEach(activity -> {
            assertThat(activity.getHour()).isBetween(0, 23);
            assertThat(activity.getWinRate()).isBetween(0.0, 1.0);
        });
    }
}
