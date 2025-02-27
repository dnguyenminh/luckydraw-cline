package vn.com.fecredit.app.util;

import vn.com.fecredit.app.entity.*;
import java.time.*;
import java.util.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Test data builder for creating test entities and test scenarios
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDataBuilder {
    private static final Random RANDOM = new Random();
    private static final double DEFAULT_WIN_PROBABILITY = 0.3;

    // Forward methods to delegate
    public static TestDataBuilderDelegate.UserBuilder aUser() {
        return TestDataBuilderDelegate.aUser();
    }

    public static TestDataBuilderDelegate.RoleBuilder aRole() {
        return TestDataBuilderDelegate.aRole();
    }

    public static TestDataBuilderDelegate.EventBuilder anEvent() {
        return TestDataBuilderDelegate.anEvent();
    }

    public static Event anEventInProgress() {
        LocalDateTime now = LocalDateTime.now();
        Event event = anEvent()
            .startDate(now.minusDays(1))
            .endDate(now.plusDays(6))
            .isActive(true)
            .build();
        event.setRemainingSpins(event.getTotalSpins());
        return event;
    }

    public static TestDataBuilderDelegate.EventLocationBuilder anEventLocation() {
        return TestDataBuilderDelegate.anEventLocation();
    }

    public static TestDataBuilderDelegate.ParticipantBuilder aParticipant() {
        return TestDataBuilderDelegate.aParticipant();
    }

    public static TestDataBuilderDelegate.SpinHistoryBuilder aSpinHistory() {
        return TestDataBuilderDelegate.aSpinHistory();
    }

    public static TestDataBuilderDelegate.RewardBuilder aReward() {
        return TestDataBuilderDelegate.aReward();
    }

    public static TestDataBuilderDelegate.GoldenHourBuilder aGoldenHour() {
        return TestDataBuilderDelegate.aGoldenHour();
    }

    // Event Scenario Generation
    public static Event aHighVolumeEvent() {
        return generateCompleteEvent(
            "High Volume Event",
            100000,  // totalSpins
            100,     // dailySpinLimit
            5,       // locationCount
            20000,   // spinsPerLocation
            2.0,     // maxLocationMultiplier
            6,       // goldenHourCount
            3.0,     // maxGoldenHourMultiplier
            1000     // participantCount
        );
    }

    public static Event aLowWinRateEvent() {
        return generateCompleteEvent(
            "Low Win Rate Event",
            10000,   // totalSpins
            20,      // dailySpinLimit
            3,       // locationCount
            3000,    // spinsPerLocation
            1.5,     // maxLocationMultiplier
            2,       // goldenHourCount
            2.0,     // maxGoldenHourMultiplier
            100      // participantCount
        );
    }

    private static Event generateCompleteEvent(
            String name, int totalSpins, int dailySpinLimit,
            int locationCount, int spinsPerLocation, double maxLocationMultiplier,
            int goldenHourCount, double maxGoldenHourMultiplier, int participantCount) {

        Event event = anEventInProgress();
        event.setName(name);
        event.setTotalSpins(totalSpins);
        event.setRemainingSpins(totalSpins);
        event.setDailySpinLimit(dailySpinLimit);

        addLocationsToEvent(event, locationCount, spinsPerLocation, dailySpinLimit, maxLocationMultiplier);
        addRewardsToEvent(event);
        addGoldenHoursToEvent(event, goldenHourCount, maxGoldenHourMultiplier);
        generateSpinHistory(event, participantCount, dailySpinLimit);

        return event;
    }

    private static void addLocationsToEvent(Event event, int locationCount, 
            int spinsPerLocation, int dailySpinLimit, double maxLocationMultiplier) {
        for (int i = 0; i < locationCount; i++) {
            EventLocation location = anEventLocation()
                .name(String.format("%s - Location %d", event.getName(), i + 1))
                .totalSpins(spinsPerLocation)
                .dailySpinLimit(dailySpinLimit)
                .winProbabilityMultiplier(1.0 + RANDOM.nextDouble() * maxLocationMultiplier)
                .build();
            event.addLocation(location);
            TestEntitySetter.setLocationEvent(location, event);
        }
    }

    private static void addRewardsToEvent(Event event) {
        List<Object[]> tiers = Arrays.asList(
            new Object[]{"Grand Prize", 0.001, 5, 1},
            new Object[]{"Major Prize", 0.01, 50, 2},
            new Object[]{"Medium Prize", 0.1, 500, 10},
            new Object[]{"Small Prize", 0.5, 5000, 50}
        );

        for (Object[] tier : tiers) {
            Reward reward = aReward()
                .name((String) tier[0])
                .winProbability((Double) tier[1])
                .quantity((Integer) tier[2])
                .dailyLimit((Integer) tier[3])
                .build();
            event.addReward(reward);
            TestEntitySetter.setRewardEvent(reward, event);
        }
    }

    private static void addGoldenHoursToEvent(Event event, int goldenHourCount, double maxGoldenHourMultiplier) {
        int hoursPerDay = 24 / goldenHourCount;
        for (int i = 0; i < goldenHourCount; i++) {
            LocalTime startTime = LocalTime.of(i * hoursPerDay, 0);
            LocalTime endTime = LocalTime.of(i * hoursPerDay + 2, 0);

            GoldenHour goldenHour = aGoldenHour()
                .name(String.format("%s - Golden Hour %d", event.getName(), i + 1))
                .startTime(startTime)
                .endTime(endTime)
                .probabilityMultiplier(1.0 + RANDOM.nextDouble() * maxGoldenHourMultiplier)
                .build();
            event.addGoldenHour(goldenHour);
            goldenHour.setEvent(event);
        }
    }

    private static void generateSpinHistory(Event event, int participantCount, int dailySpinLimit) {
        LocalDateTime startDate = event.getStartDate();
        LocalDateTime endDate = event.getEndDate();
        long durationHours = Duration.between(startDate, endDate).toHours();

        for (int i = 0; i < participantCount; i++) {
            Participant participant = aParticipant().build();
            event.addParticipant(participant);
            TestEntitySetter.setParticipantEvent(participant, event);

            int spinCount = RANDOM.nextInt(dailySpinLimit * 7);
            for (int j = 0; j < spinCount; j++) {
                SpinHistory spin = generateSingleSpin(event, participant, startDate, durationHours);
                event.addSpinHistory(spin);
                participant.addSpinHistory(spin);

                if (!event.getLocations().isEmpty()) {
                    EventLocation location = event.getLocations().get(RANDOM.nextInt(event.getLocations().size()));
                    location.addSpinHistory(spin);
                    TestEntitySetter.setSpinLocation(spin, location);
                }
            }
        }

        event.setRemainingSpins(event.getTotalSpins() - event.getSpinHistories().size());
    }

    private static SpinHistory generateSingleSpin(Event event, Participant participant,
            LocalDateTime startDate, long durationHours) {
        LocalDateTime spinTime = startDate.plusHours(RANDOM.nextInt((int) durationHours));
        boolean isWin = RANDOM.nextDouble() < DEFAULT_WIN_PROBABILITY;
        boolean isGoldenHour = checkGoldenHourActive(event, spinTime);

        SpinHistory spin = aSpinHistory()
            .spinTime(spinTime)
            .isWin(isWin)
            .isGoldenHourActive(isGoldenHour)
            .probabilityMultiplier(getProbabilityMultiplier(event, spinTime))
            .build();

        if (isWin && !event.getRewards().isEmpty()) {
            Reward reward = selectRandomReward(event.getRewards());
            TestEntitySetter.setSpinReward(spin, reward);
        }

        TestEntitySetter.setSpinEvent(spin, event);
        TestEntitySetter.setSpinParticipant(spin, participant);
        return spin;
    }

    private static boolean checkGoldenHourActive(Event event, LocalDateTime time) {
        return event.getGoldenHours().stream()
            .anyMatch(gh -> gh.getActiveDays().contains(time.getDayOfWeek()) &&
                           isTimeBetween(time.toLocalTime(), gh.getStartTime(), gh.getEndTime()));
    }

    private static boolean isTimeBetween(LocalTime time, LocalTime start, LocalTime end) {
        return !time.isBefore(start) && !time.isAfter(end);
    }

    private static double getProbabilityMultiplier(Event event, LocalDateTime time) {
        Optional<GoldenHour> activeGoldenHour = event.getGoldenHours().stream()
            .filter(gh -> gh.getActiveDays().contains(time.getDayOfWeek()) &&
                         isTimeBetween(time.toLocalTime(), gh.getStartTime(), gh.getEndTime()))
            .findFirst();

        return activeGoldenHour.map(GoldenHour::getProbabilityMultiplier).orElse(1.0);
    }

    private static Reward selectRandomReward(List<Reward> rewards) {
        double totalProbability = rewards.stream()
            .mapToDouble(Reward::getWinProbability)
            .sum();

        double random = RANDOM.nextDouble() * totalProbability;
        double cumulative = 0.0;

        for (Reward reward : rewards) {
            cumulative += reward.getWinProbability();
            if (random <= cumulative) {
                return reward;
            }
        }

        return rewards.get(rewards.size() - 1);
    }

    public static void resetCounter() {
        TestDataBuilderDelegate.resetCounter();
    }
}
