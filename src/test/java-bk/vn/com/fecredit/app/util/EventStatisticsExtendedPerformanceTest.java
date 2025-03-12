package vn.com.fecredit.app.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import vn.com.fecredit.app.entity.*;

class EventStatisticsExtendedPerformanceTest {
    private Event largeEvent;
    private static final int PARTICIPANT_COUNT = 1000;
    private static final int LOCATION_COUNT = 50;
    private static final int REWARD_COUNT = 20;
    private static final int SPIN_COUNT = 100_000;
    private Random random;

    @BeforeEach
    void setUp() {
        TestDataBuilder.resetCounter();
        random = new Random(42); // Fixed seed for reproducibility
        largeEvent = generateLargeEvent();
    }

    private Event generateLargeEvent() {
        Event event = TestDataBuilder.anEvent()
            .name("Large Performance Test Event")
            .startDate(LocalDateTime.now().minusDays(30))
            .endDate(LocalDateTime.now().plusDays(30))
            .isActive(true)
            .build();

        // Generate locations with different multipliers
        List<EventLocation> locations = new ArrayList<>();
        for (int i = 0; i < LOCATION_COUNT; i++) {
            EventLocation location = TestDataBuilder.anEventLocation()
                .name("Location " + i)
                .winProbabilityMultiplier(1.0 + (i * 0.1))
                .build();
            event.addLocation(location);
            TestEntitySetter.setLocationEvent(location, event);
            locations.add(location);
        }

        // Generate rewards with varying probabilities
        List<Reward> rewards = new ArrayList<>();
        for (int i = 0; i < REWARD_COUNT; i++) {
            Reward reward = TestDataBuilder.aReward()
                .name("Reward " + i)
                .winProbability(0.1 / (i + 1))
                .quantity(1000)
                .dailyLimit(100)
                .build();
            event.addReward(reward);
            TestEntitySetter.setRewardEvent(reward, event);
            rewards.add(reward);
        }

        // Generate participants
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < PARTICIPANT_COUNT; i++) {
            Participant participant = TestDataBuilder.aParticipant()
                .customerId("PERF" + String.format("%04d", i))
                .build();
            event.addParticipant(participant);
            participants.add(participant);
        }

        // Generate spins
        LocalDateTime baseTime = event.getStartDate();
        for (int i = 0; i < SPIN_COUNT; i++) {
            EventLocation location = locations.get(random.nextInt(locations.size()));
            Participant participant = participants.get(random.nextInt(participants.size()));
            boolean isWin = random.nextDouble() < 0.3;
            Reward reward = isWin ? rewards.get(random.nextInt(rewards.size())) : null;
            
            LocalDateTime spinTime = baseTime.plusMinutes(random.nextInt(60 * 24 * 60));
            SpinHistory spin = TestDataBuilder.aSpinHistory()
                .spinTime(spinTime)
                .isWin(isWin)
                .build();

            TestEntitySetter.setSpinEvent(spin, event);
            TestEntitySetter.setSpinLocation(spin, location);
            TestEntitySetter.setSpinParticipant(spin, participant);
            if (isWin) {
                TestEntitySetter.setSpinReward(spin, reward);
            }

            event.addSpinHistory(spin);
            location.addSpinHistory(spin);
        }

        return event;
    }

    @Test
    void analyzeRewards_ShouldHandleLargeDatasetEfficiently() {
        long startTime = System.nanoTime();
        Map<String, EventStatisticsExtended.RewardStats> stats = 
            EventStatisticsExtended.analyzeRewards(largeEvent);
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        assertThat(duration).isLessThan(1000); // Should complete within 1 second
        assertThat(stats).hasSize(REWARD_COUNT);

        // Verify stats calculations are accurate
        stats.values().forEach(rewardStats -> {
            assertThat(rewardStats.getTotalWins()).isPositive();
            assertThat(rewardStats.getWinRate()).isBetween(0.0, 1.0);
            assertThat(rewardStats.getAvgTimeGap()).isPositive();
        });
    }

    @Test
    void analyzeParticipants_ShouldHandleLargeDatasetEfficiently() {
        long startTime = System.nanoTime();
        Map<String, EventStatisticsExtended.ParticipantStats> stats = 
            EventStatisticsExtended.analyzeParticipants(largeEvent);
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        assertThat(duration).isLessThan(1000);
        assertThat(stats).hasSize(PARTICIPANT_COUNT);

        // Verify participant statistics
        stats.values().forEach(participantStats -> {
            assertThat(participantStats.getTotalSpins()).isPositive();
            assertThat(participantStats.getWinRate()).isBetween(0.0, 1.0);
            assertThat(participantStats.getActiveHours()).hasSizeLessThanOrEqualTo(24);
        });
    }

    @Test
    void analyzeLocations_ShouldHandleLargeDatasetEfficiently() {
        long startTime = System.nanoTime();
        Map<String, EventStatisticsExtended.LocationStats> stats = 
            EventStatisticsExtended.analyzeLocations(largeEvent);
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        assertThat(duration).isLessThan(1000);
        assertThat(stats).hasSize(LOCATION_COUNT);

        // Verify location statistics
        stats.values().forEach(locationStats -> {
            assertThat(locationStats.getTotalSpins()).isPositive();
            assertThat(locationStats.getUniqueParticipants()).isPositive();
            assertThat(locationStats.getHourlyActivity()).hasSize(24);
        });
    }

    @Test
    void memoryUsage_ShouldBeReasonable() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Run all analyses
        EventStatisticsExtended.analyzeRewards(largeEvent);
        EventStatisticsExtended.analyzeParticipants(largeEvent);
        EventStatisticsExtended.analyzeLocations(largeEvent);
        
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = usedMemoryAfter - usedMemoryBefore;
        
        // Memory increase should be less than 100MB for 100K spins
        assertThat(memoryIncrease).isLessThan(100L * 1024 * 1024);
    }

    @Test
    void concurrentAccess_ShouldHandleMultipleThreads() throws Exception {
        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        // Submit analysis tasks
        futures.add(executor.submit(() -> {
            EventStatisticsExtended.analyzeRewards(largeEvent);
            latch.countDown();
        }));
        futures.add(executor.submit(() -> {
            EventStatisticsExtended.analyzeParticipants(largeEvent);
            latch.countDown();
        }));
        futures.add(executor.submit(() -> {
            EventStatisticsExtended.analyzeLocations(largeEvent);
            latch.countDown();
        }));
        futures.add(executor.submit(() -> {
            EventStatisticsExtended.analyzeRewards(largeEvent);
            latch.countDown();
        }));

        // Wait for all tasks to complete
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        futures.forEach(future -> assertThat(future).isDone());
    }
}
