package vn.com.fecredit.app.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.*;
import java.util.*;
import vn.com.fecredit.app.entity.*;

class EventStatisticsPerformanceTest {
    private Event largeEvent;
    private static final int SPIN_COUNT = 100_000;
    private static final int LOCATION_COUNT = 10;
    private static final int REWARD_COUNT = 5;

    @BeforeEach
    void setUp() {
        TestDataBuilder.resetCounter();
        largeEvent = generateLargeEvent();
    }

    private Event generateLargeEvent() {
        Event event = TestDataBuilder.anEvent()
            .name("Large Performance Test Event")
            .startDate(LocalDateTime.now().minusDays(30))
            .endDate(LocalDateTime.now().plusDays(30))
            .isActive(true)
            .build();

        // Add multiple locations
        for (int i = 0; i < LOCATION_COUNT; i++) {
            EventLocation location = TestDataBuilder.anEventLocation()
                .name("Location " + i)
                .winProbabilityMultiplier(1.0 + (i * 0.1))
                .build();
            event.addLocation(location);
            TestEntitySetter.setLocationEvent(location, event);
        }

        // Add multiple rewards
        for (int i = 0; i < REWARD_COUNT; i++) {
            Reward reward = TestDataBuilder.aReward()
                .name("Reward " + i)
                .winProbability(0.1 / (i + 1))
                .quantity(1000)
                .dailyLimit(100)
                .build();
            event.addReward(reward);
            TestEntitySetter.setRewardEvent(reward, event);
        }

        // Generate large number of spins
        Random random = new Random();
        List<EventLocation> locations = event.getLocations();
        List<Reward> rewards = event.getRewards();

        for (int i = 0; i < SPIN_COUNT; i++) {
            EventLocation location = locations.get(random.nextInt(locations.size()));
            boolean isWin = random.nextDouble() < 0.3;
            
            SpinHistory spin = TestDataBuilder.aSpinHistory()
                .spinTime(event.getStartDate().plusMinutes(random.nextInt(60 * 24 * 60))) // Random time within event
                .isWin(isWin)
                .build();

            TestEntitySetter.setSpinEvent(spin, event);
            TestEntitySetter.setSpinLocation(spin, location);
            
            if (isWin) {
                TestEntitySetter.setSpinReward(spin, rewards.get(random.nextInt(rewards.size())));
            }

            event.addSpinHistory(spin);
            location.addSpinHistory(spin);
        }

        return event;
    }

    @Test
    void analyze_ShouldCompleteQuicklyForLargeDataset() {
        long startTime = System.nanoTime();
        EventStatistics.EventAnalysis analysis = EventStatistics.analyze(largeEvent);
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        assertThat(durationMs).isLessThan(1000); // Should complete in under 1 second
        
        // Verify analysis contains expected data
        assertThat(analysis.getHourlyWinRates()).hasSize(24);
        assertThat(analysis.getRewardDistribution()).hasSize(REWARD_COUNT);
    }

    @Test
    void calculateLocationWinRates_ShouldPerformWellWithLargeDataset() {
        long startTime = System.nanoTime();
        Map<String, Double> rates = EventStatistics.calculateLocationWinRates(largeEvent);
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;
        assertThat(durationMs).isLessThan(500); // Should complete in under 500ms
        assertThat(rates).hasSize(LOCATION_COUNT);
    }

    @Test
    void analyzeHourlyActivity_ShouldHandleLargeDatasetEfficiently() {
        long startTime = System.nanoTime();
        List<EventStatistics.HourlyActivity> activities = EventStatistics.analyzeHourlyActivity(largeEvent);
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;
        assertThat(durationMs).isLessThan(500);
        assertThat(activities).hasSizeGreaterThan(0);
    }

    @Test
    void calculateDailyWinRates_ShouldScaleWithLargeDataset() {
        long startTime = System.nanoTime();
        Map<DayOfWeek, Double> dailyRates = EventStatistics.calculateDailyWinRates(largeEvent);
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;
        assertThat(durationMs).isLessThan(500);
        assertThat(dailyRates).isNotEmpty();
    }

    @Test
    void memoryUsage_ShouldRemainReasonable() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        EventStatistics.EventAnalysis analysis = EventStatistics.analyze(largeEvent);
        
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = usedMemoryAfter - usedMemoryBefore;
        
        // Memory increase should be less than 100MB for 100K spins
        assertThat(memoryIncrease).isLessThan(100 * 1024 * 1024);
    }
}
