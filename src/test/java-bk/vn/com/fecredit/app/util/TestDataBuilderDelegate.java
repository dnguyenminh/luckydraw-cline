package vn.com.fecredit.app.util;

import vn.com.fecredit.app.entity.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Delegate class that provides static access to builder methods
 */
public class TestDataBuilderDelegate {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public static class UserBuilder {
        private final User.UserBuilder builder;

        UserBuilder() {
            int count = COUNTER.incrementAndGet();
            builder = User.builder()
                .username("testuser" + count)
                .password("password" + count)
                .email("test" + count + "@example.com")
                .fullName("Test User " + count)
                .phoneNumber("+8412345" + String.format("%04d", count))
                .isActive(true)
                .status(User.UserStatus.ACTIVE)
                .roles(new HashSet<>());
        }

        public User build() {
            return builder.build();
        }

        public UserBuilder fullName(String name) {
            builder.fullName(name);
            return this;
        }
    }

    public static class RoleBuilder {
        private final Role.RoleBuilder builder;

        RoleBuilder() {
            int count = COUNTER.incrementAndGet();
            builder = Role.builder()
                .name(RoleName.ROLE_PARTICIPANT)
                .description("Test Role " + count)
                .users(new HashSet<>());
        }

        public Role build() {
            return builder.build();
        }

        public RoleBuilder name(RoleName name) {
            builder.name(name);
            return this;
        }
    }

    public static class EventBuilder {
        private final Event.EventBuilder builder;

        EventBuilder() {
            int count = COUNTER.incrementAndGet();
            LocalDateTime now = LocalDateTime.now();
            builder = Event.builder()
                .name("Test Event " + count)
                .description("Test Event Description " + count)
                .startDate(now)
                .endDate(now.plusDays(7))
                .totalSpins(1000)
                .remainingSpins(1000)
                .dailySpinLimit(5)
                .isActive(true)
                .participants(new ArrayList<>())
                .locations(new ArrayList<>())
                .rewards(new ArrayList<>())
                .goldenHours(new ArrayList<>())
                .spinHistories(new ArrayList<>())
                .deleted(false);
        }

        public Event build() {
            return builder.build();
        }

        public EventBuilder startDate(LocalDateTime startDate) {
            builder.startDate(startDate);
            return this;
        }

        public EventBuilder endDate(LocalDateTime endDate) {
            builder.endDate(endDate);
            return this;
        }

        public EventBuilder isActive(boolean active) {
            builder.isActive(active);
            return this;
        }

        public EventBuilder name(String name) {
            builder.name(name);
            return this;
        }

        public EventBuilder dailySpinLimit(int limit) {
            builder.dailySpinLimit(limit);
            return this;
        }

        public EventBuilder totalSpins(int total) {
            builder.totalSpins(total);
            builder.remainingSpins(total);
            return this;
        }

        public EventBuilder deleted(boolean deleted) {
            builder.deleted(deleted);
            return this;
        }
    }

    public static class EventLocationBuilder {
        private final EventLocation.EventLocationBuilder builder;

        EventLocationBuilder() {
            int count = COUNTER.incrementAndGet();
            builder = EventLocation.builder()
                .name("Test Location " + count)
                .province("Test Province " + count)
                .addressLine1("123 Test Street")
                .addressLine2("Unit " + count)
                .city("Test City")
                .postalCode("10000" + count)
                .totalSpins(1000)
                .remainingSpins(1000)
                .dailySpinLimit(10)
                .winProbabilityMultiplier(1.0)
                .isActive(true)
                .participants(new ArrayList<>())
                .spinHistories(new ArrayList<>())
                .deleted(false);
        }

        public EventLocation build() {
            return builder.build();
        }

        public EventLocationBuilder name(String name) {
            builder.name(name);
            return this;
        }

        public EventLocationBuilder totalSpins(int total) {
            builder.totalSpins(total);
            builder.remainingSpins(total);
            return this;
        }

        public EventLocationBuilder dailySpinLimit(int limit) {
            builder.dailySpinLimit(limit);
            return this;
        }

        public EventLocationBuilder winProbabilityMultiplier(double multiplier) {
            builder.winProbabilityMultiplier(multiplier);
            return this;
        }
    }

    public static class RewardBuilder {
        private final Reward.RewardBuilder builder;

        RewardBuilder() {
            int count = COUNTER.incrementAndGet();
            builder = Reward.builder()
                .name("Test Reward " + count)
                .description("Test Reward Description " + count)
                .code("RWD" + String.format("%06d", count))
                .totalQuantity(100)
                .remainingQuantity(100)
                .dailyLimit(10)
                .winProbability(0.1)
                .isActive(true)
                .spinHistories(new ArrayList<>())
                .deleted(false);
        }

        public Reward build() {
            return builder.build();
        }

        public RewardBuilder name(String name) {
            builder.name(name);
            return this;
        }

        public RewardBuilder winProbability(double probability) {
            builder.winProbability(probability);
            return this;
        }

        public RewardBuilder quantity(int total) {
            builder.totalQuantity(total);
            builder.remainingQuantity(total);
            return this;
        }

        public RewardBuilder dailyLimit(int limit) {
            builder.dailyLimit(limit);
            return this;
        }
    }

    public static class GoldenHourBuilder {
        private final GoldenHour.GoldenHourBuilder builder;

        GoldenHourBuilder() {
            int count = COUNTER.incrementAndGet();
            Set<DayOfWeek> activeDays = new HashSet<>();
            activeDays.add(DayOfWeek.MONDAY);
            activeDays.add(DayOfWeek.FRIDAY);

            builder = GoldenHour.builder()
                .name("Test Golden Hour " + count)
                .description("Test Golden Hour Description " + count)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .activeDays(activeDays)
                .probabilityMultiplier(2.0)
                .isActive(true)
                .spinHistories(new HashSet<>())
                .deleted(false);
        }

        public GoldenHour build() {
            return builder.build();
        }

        public GoldenHourBuilder name(String name) {
            builder.name(name);
            return this;
        }

        public GoldenHourBuilder startTime(LocalTime time) {
            builder.startTime(time);
            return this;
        }

        public GoldenHourBuilder endTime(LocalTime time) {
            builder.endTime(time);
            return this;
        }

        public GoldenHourBuilder probabilityMultiplier(double multiplier) {
            builder.probabilityMultiplier(multiplier);
            return this;
        }
    }

    public static class SpinHistoryBuilder {
        private final SpinHistory.SpinHistoryBuilder builder;

        SpinHistoryBuilder() {
            builder = SpinHistory.builder()
                .spinTime(LocalDateTime.now())
                .isWin(false)
                .isGoldenHourActive(false)
                .probabilityMultiplier(1.0)
                .ipAddress("127.0.0.1")
                .userAgent("Test User Agent")
                .deleted(false);
        }

        public SpinHistory build() {
            return builder.build();
        }

        public SpinHistoryBuilder spinTime(LocalDateTime time) {
            builder.spinTime(time);
            return this;
        }

        public SpinHistoryBuilder isWin(boolean isWin) {
            builder.isWin(isWin);
            return this;
        }

        public SpinHistoryBuilder isGoldenHourActive(boolean active) {
            builder.isGoldenHourActive(active);
            return this;
        }

        public SpinHistoryBuilder probabilityMultiplier(double multiplier) {
            builder.probabilityMultiplier(multiplier);
            return this;
        }
    }

    public static class ParticipantBuilder {
        private final Participant.ParticipantBuilder builder;

        ParticipantBuilder() {
            int count = COUNTER.incrementAndGet();
            builder = Participant.builder()
                .customerId("CUS" + String.format("%06d", count))
                .cardNumber("CARD" + String.format("%06d", count))
                .fullName("Test Participant " + count)
                .email("participant" + count + "@example.com")
                .phoneNumber("+8412345" + String.format("%04d", count))
                .province("Test Province")
                .spinsRemaining(10)
                .dailySpinLimit(5)
                .lastSpinDate(LocalDateTime.now().minusDays(1))
                .isActive(true)
                .roles(new HashSet<>())
                .spinHistories(new ArrayList<>())
                .deleted(false);
        }

        public Participant build() {
            return builder.build();
        }

        public ParticipantBuilder customerId(String id) {
            builder.customerId(id);
            return this;
        }

        public ParticipantBuilder cardNumber(String number) {
            builder.cardNumber(number);
            return this;
        }

        public ParticipantBuilder fullName(String name) {
            builder.fullName(name);
            return this;
        }

        public ParticipantBuilder email(String email) {
            builder.email(email);
            return this;
        }

        public ParticipantBuilder phoneNumber(String phone) {
            builder.phoneNumber(phone);
            return this;
        }

        public ParticipantBuilder spinsRemaining(int spins) {
            builder.spinsRemaining(spins);
            return this;
        }
    }

    // Static factory methods
    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    public static RoleBuilder aRole() {
        return new RoleBuilder();
    }

    public static EventBuilder anEvent() {
        return new EventBuilder();
    }

    public static RewardBuilder aReward() {
        return new RewardBuilder();
    }

    public static GoldenHourBuilder aGoldenHour() {
        return new GoldenHourBuilder();
    }

    public static ParticipantBuilder aParticipant() {
        return new ParticipantBuilder();
    }

    public static EventLocationBuilder anEventLocation() {
        return new EventLocationBuilder();
    }

    public static SpinHistoryBuilder aSpinHistory() {
        return new SpinHistoryBuilder();
    }

    public static void resetCounter() {
        COUNTER.set(0);
    }
}
