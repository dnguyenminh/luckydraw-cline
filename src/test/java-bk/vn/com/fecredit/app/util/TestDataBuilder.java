package vn.com.fecredit.app.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.enums.RoleName;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDataBuilder {
    private static final AtomicLong ID_COUNTER = new AtomicLong(1);

    public static void resetCounter() {
        ID_COUNTER.set(1);
    }

    public static Event anEvent() {
        return Event.builder()
            .id(ID_COUNTER.getAndIncrement())
            .name("Test Event " + ID_COUNTER.get())
            .description("Test Description")
            .startDate(LocalDateTime.now().minusDays(1))
            .endDate(LocalDateTime.now().plusDays(1))
            .dailySpinLimit(10)
            .totalSpinLimit(100)
            .spinHistories(new HashSet<>())
            .participants(new HashSet<>())
            .locations(new HashSet<>())
            .rewards(new HashSet<>())
            .goldenHours(new HashSet<>())
            .active(true)
            .status(EntityStatus.ACTIVE)
            .build();
    }

    public static EventLocation anEventLocation() {
        return EventLocation.builder()
            .id(ID_COUNTER.getAndIncrement())
            .name("Test Location " + ID_COUNTER.get())
            .description("Test Location Description")
            .city("Test City")
            .district("Test District")
            .latitude(10.0)
            .longitude(106.0)
            .radiusMeters(100)
            .dailySpinLimit(5)
            .winProbabilityMultiplier(1.0)
            .active(true)
            .build();
    }

    public static Participant aParticipant() {
        return Participant.builder()
            .id(ID_COUNTER.getAndIncrement())
            .fullName("Test User " + ID_COUNTER.get())
            .email("test" + ID_COUNTER.get() + "@example.com")
            .deviceId("device-" + ID_COUNTER.get())
            .sessionId("session-" + ID_COUNTER.get())
            .spinHistories(new HashSet<>())
            .active(true)
            .status(EntityStatus.ACTIVE)
            .build();
    }

    public static Reward aReward() {
        return Reward.builder()
            .id(ID_COUNTER.getAndIncrement())
            .code("RWD-" + ID_COUNTER.get())
            .name("Test Reward " + ID_COUNTER.get())
            .description("Test Reward Description")
            .totalQuantity(100)
            .remainingQuantity(100)
            .winProbability(0.5)
            .spinHistories(new HashSet<>())
            .active(true)
            .status(EntityStatus.ACTIVE)
            .build();
    }

    public static SpinHistory aSpinHistory() {
        return SpinHistory.builder()
            .id(ID_COUNTER.getAndIncrement())
            .spinTime(LocalDateTime.now())
            .win(false)
            .winProbability(0.5)
            .probabilityMultiplier(1.0)
            .finalProbability(0.5)
            .active(true)
            .build();
    }

    public static GoldenHour aGoldenHour() {
        return GoldenHour.builder()
            .id(ID_COUNTER.getAndIncrement())
            .startTime(LocalDateTime.now())
            .endTime(LocalDateTime.now().plusHours(1))
            .multiplier(2.0)
            .active(true)
            .build();
    }

    public static Role aRole() {
        return Role.builder()
            .id(ID_COUNTER.getAndIncrement())
            .name(RoleName.VIEWER)
            .description("Test Role Description")
            .active(true)
            .status(EntityStatus.ACTIVE)
            .build();
    }

    public static User aUser() {
        return User.builder()
            .id(ID_COUNTER.getAndIncrement())
            .username("testuser" + ID_COUNTER.get())
            .email("testuser" + ID_COUNTER.get() + "@example.com")
            .password("password")
            .firstName("Test")
            .lastName("User")
            .roles(new HashSet<>())
            .active(true)
            .status(EntityStatus.ACTIVE)
            .build();
    }

    public static BlacklistedToken aBlacklistedToken() {
        return BlacklistedToken.builder()
            .id(ID_COUNTER.getAndIncrement())
            .tokenHash("token-hash-" + ID_COUNTER.get())
            .expiryDate(LocalDateTime.now().plusDays(1))
            .build();
    }
}
