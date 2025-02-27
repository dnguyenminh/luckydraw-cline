package vn.com.fecredit.app.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import vn.com.fecredit.app.entity.*;

class EventStatisticsExtendedBenchmarkTest {
    private static final int BASE_SPIN_COUNT = 10_000;
    private static final int WARMUP_ITERATIONS = 3;
    private static final int BENCHMARK_ITERATIONS = 5;
    private Random random;

    @BeforeEach
    void setUp() {
        TestDataBuilder.resetCounter();
        random = new Random(42);
    }

    @ParameterizedTest
    @ValueSource(ints = {1_000, 10_000, 100_000, 1_000_000})
    void benchmarkAnalyzeRewards(int spinCount) {
        Event event = generateTestEvent(spinCount);
        
        // Warm up
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            EventStatisticsExtended.analyzeRewards(event);
        }

        // Benchmark
        List<Long> durations = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.nanoTime();
            EventStatisticsExtended.analyzeRewards(event);
            durations.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
        }

        // Calculate statistics
        BenchmarkResult result = calculateStats(durations);
        System.out.printf("Reward Analysis (%d spins): avg=%.2fms, min=%dms, max=%dms, stddev=%.2fms%n",
            spinCount, result.average, result.min, result.max, result.stdDev);

        // Verify performance scales linearly(ish)
        double expectedMaxTime = (spinCount / BASE_SPIN_COUNT) * 100; // 100ms base time
        assertThat(result.average).isLessThan(expectedMaxTime);
    }

    @ParameterizedTest
    @ValueSource(ints = {1_000, 10_000, 100_000, 1_000_000})
    void benchmarkAnalyzeParticipants(int spinCount) {
        Event event = generateTestEvent(spinCount);
        
        // Warm up
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            EventStatisticsExtended.analyzeParticipants(event);
        }

        List<Long> durations = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.nanoTime();
            EventStatisticsExtended.analyzeParticipants(event);
            durations.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
        }

        BenchmarkResult result = calculateStats(durations);
        System.out.printf("Participant Analysis (%d spins): avg=%.2fms, min=%dms, max=%dms, stddev=%.2fms%n",
            spinCount, result.average, result.min, result.max, result.stdDev);

        double expectedMaxTime = (spinCount / BASE_SPIN_COUNT) * 100;
        assertThat(result.average).isLessThan(expectedMaxTime);
    }

    @Test
    void benchmarkScalability() {
        int[] spinCounts = {1_000, 10_000, 100_000};
        Map<Integer, Double> timePerSpin = new HashMap<>();

        for (int spinCount : spinCounts) {
            Event event = generateTestEvent(spinCount);
            
            // Warm up
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                EventStatisticsExtended.analyzeRewards(event);
            }

            // Measure
            long start = System.nanoTime();
            for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
                EventStatisticsExtended.analyzeRewards(event);
            }
            long totalTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            
            double avgTimePerSpin = (double) totalTime / (spinCount * BENCHMARK_ITERATIONS);
            timePerSpin.put(spinCount, avgTimePerSpin);
        }

        // Verify near-linear scaling (allowing for some variance)
        double baseTimePerSpin = timePerSpin.get(1_000);
        timePerSpin.forEach((spins, time) -> {
            double ratio = time / baseTimePerSpin;
            assertThat(ratio).isBetween(0.5, 2.0); // Allow 2x variance in per-spin time
        });
    }

    @Test
    void benchmarkMemoryEfficiency() {
        Runtime runtime = Runtime.getRuntime();
        List<Long> memoryUsage = new ArrayList<>();
        int[] spinCounts = {10_000, 100_000, 1_000_000};

        for (int spinCount : spinCounts) {
            Event event = generateTestEvent(spinCount);
            System.gc(); // Suggest garbage collection

            long beforeMem = runtime.totalMemory() - runtime.freeMemory();
            EventStatisticsExtended.analyzeRewards(event);
            EventStatisticsExtended.analyzeParticipants(event);
            EventStatisticsExtended.analyzeLocations(event);
            long afterMem = runtime.totalMemory() - runtime.freeMemory();
            
            long memoryIncrease = afterMem - beforeMem;
            memoryUsage.add(memoryIncrease);

            System.out.printf("Memory usage for %d spins: %.2f MB%n", 
                spinCount, memoryIncrease / (1024.0 * 1024.0));
        }

        // Verify sub-linear memory growth
        for (int i = 1; i < memoryUsage.size(); i++) {
            double ratio = (double) memoryUsage.get(i) / memoryUsage.get(i-1);
            assertThat(ratio).isLessThan(10.0); // Memory should not grow 10x when data grows 10x
        }
    }

    private static class BenchmarkResult {
        final double average;
        final long min;
        final long max;
        final double stdDev;

        BenchmarkResult(double average, long min, long max, double stdDev) {
            this.average = average;
            this.min = min;
            this.max = max;
            this.stdDev = stdDev;
        }
    }

    private BenchmarkResult calculateStats(List<Long> durations) {
        double avg = durations.stream().mapToLong(l -> l).average().orElse(0.0);
        long min = durations.stream().mapToLong(l -> l).min().orElse(0);
        long max = durations.stream().mapToLong(l -> l).max().orElse(0);
        
        double variance = durations.stream()
            .mapToDouble(d -> Math.pow(d - avg, 2))
            .average()
            .orElse(0.0);
        double stdDev = Math.sqrt(variance);

        return new BenchmarkResult(avg, min, max, stdDev);
    }

    private Event generateTestEvent(int spinCount) {
        Event event = TestDataBuilder.anEvent()
            .name("Benchmark Event")
            .startDate(LocalDateTime.now().minusDays(30))
            .endDate(LocalDateTime.now().plusDays(30))
            .isActive(true)
            .build();

        int locationCount = Math.min(50, spinCount / 100);
        int participantCount = Math.min(1000, spinCount / 50);
        int rewardCount = Math.min(20, spinCount / 1000);

        List<EventLocation> locations = generateLocations(event, locationCount);
        List<Participant> participants = generateParticipants(event, participantCount);
        List<Reward> rewards = generateRewards(event, rewardCount);

        generateSpins(event, locations, participants, rewards, spinCount);

        return event;
    }

    private List<EventLocation> generateLocations(Event event, int count) {
        List<EventLocation> locations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            EventLocation location = TestDataBuilder.anEventLocation()
                .name("Location " + i)
                .winProbabilityMultiplier(1.0 + (i * 0.1))
                .build();
            event.addLocation(location);
            TestEntitySetter.setLocationEvent(location, event);
            locations.add(location);
        }
        return locations;
    }

    private List<Participant> generateParticipants(Event event, int count) {
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Participant participant = TestDataBuilder.aParticipant()
                .customerId("BENCH" + String.format("%04d", i))
                .build();
            event.addParticipant(participant);
            participants.add(participant);
        }
        return participants;
    }

    private List<Reward> generateRewards(Event event, int count) {
        List<Reward> rewards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Reward reward = TestDataBuilder.aReward()
                .name("Reward " + i)
                .winProbability(0.1 / (i + 1))
                .quantity(10000)
                .dailyLimit(1000)
                .build();
            event.addReward(reward);
            TestEntitySetter.setRewardEvent(reward, event);
            rewards.add(reward);
        }
        return rewards;
    }

    private void generateSpins(Event event, List<EventLocation> locations, 
                             List<Participant> participants, List<Reward> rewards, 
                             int spinCount) {
        LocalDateTime baseTime = event.getStartDate();
        for (int i = 0; i < spinCount; i++) {
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
    }
}
