package vn.com.fecredit.app.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import vn.com.fecredit.app.entity.*;
import vn.com.fecredit.app.exception.BusinessException;

class EventStatisticsExtendedNegativeTest {
    private Event event;
    private EventLocation location;
    private Reward reward;
    private Participant participant;

    @BeforeEach
    void setUp() {
        TestDataBuilder.resetCounter();
        event = TestDataBuilder.anEvent()
            .name("Negative Test Event")
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
            .quantity(10)
            .dailyLimit(2)
            .build();

        participant = TestDataBuilder.aParticipant()
            .customerId("TEST001")
            .build();
    }

    @Test
    void analyzeRewards_ShouldHandleNullEvent() {
        assertThatThrownBy(() -> EventStatisticsExtended.analyzeRewards(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Event cannot be null");
    }

    @Test
    void analyzeRewards_ShouldHandleEmptyEvent() {
        Map<String, EventStatisticsExtended.RewardStats> stats = 
            EventStatisticsExtended.analyzeRewards(event);
        assertThat(stats).isEmpty();
    }

    @Test
    void analyzeRewards_ShouldHandleNullRewards() {
        event.setRewards(null);
        Map<String, EventStatisticsExtended.RewardStats> stats = 
            EventStatisticsExtended.analyzeRewards(event);
        assertThat(stats).isEmpty();
    }

    @Test
    void analyzeParticipants_ShouldHandleNullParticipants() {
        event.setParticipants(null);
        Map<String, EventStatisticsExtended.ParticipantStats> stats = 
            EventStatisticsExtended.analyzeParticipants(event);
        assertThat(stats).isEmpty();
    }

    @Test
    void analyzeLocations_ShouldHandleNullLocations() {
        event.setLocations(null);
        Map<String, EventStatisticsExtended.LocationStats> stats = 
            EventStatisticsExtended.analyzeLocations(event);
        assertThat(stats).isEmpty();
    }

    @Test
    void analyzeRewards_ShouldHandleInvalidSpinData() {
        event.addReward(reward);
        TestEntitySetter.setRewardEvent(reward, event);

        // Add spin with null time
        SpinHistory invalidSpin = TestDataBuilder.aSpinHistory()
            .spinTime(null)
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(invalidSpin, event);
        TestEntitySetter.setSpinReward(invalidSpin, reward);
        event.addSpinHistory(invalidSpin);

        Map<String, EventStatisticsExtended.RewardStats> stats = 
            EventStatisticsExtended.analyzeRewards(event);
        
        EventStatisticsExtended.RewardStats rewardStats = stats.get(reward.getName());
        assertThat(rewardStats.getTotalWins()).isEqualTo(1);
        assertThat(rewardStats.getPeakHours()).isEmpty();
        assertThat(rewardStats.getDistributionByDay()).isEmpty();
    }

    @Test
    void analyzeParticipants_ShouldHandleCorruptedData() {
        event.addParticipant(participant);
        
        // Add spin without proper participant reference
        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(LocalDateTime.now())
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(spin, event);
        event.addSpinHistory(spin);

        Map<String, EventStatisticsExtended.ParticipantStats> stats = 
            EventStatisticsExtended.analyzeParticipants(event);
        
        EventStatisticsExtended.ParticipantStats participantStats = 
            stats.get(participant.getCustomerId());
        assertThat(participantStats.getTotalSpins()).isZero();
    }

    @Test
    void analyzeLocations_ShouldHandleInvalidMultiplier() {
        location.setWinProbabilityMultiplier(-1.0);
        event.addLocation(location);
        TestEntitySetter.setLocationEvent(location, event);

        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(LocalDateTime.now())
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(spin, event);
        TestEntitySetter.setSpinLocation(spin, location);
        event.addSpinHistory(spin);
        location.addSpinHistory(spin);

        assertThatThrownBy(() -> EventStatisticsExtended.analyzeLocations(event))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Probability multiplier must be positive");
    }

    @Test
    void analyzeHourlyActivity_ShouldHandleOutOfRangeHours() {
        event.addLocation(location);
        TestEntitySetter.setLocationEvent(location, event);

        // Add spin with manipulated hour value
        LocalDateTime invalidTime = LocalDateTime.now()
            .withHour(25); // Invalid hour
        
        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(invalidTime)
            .isWin(true)
            .build();
        TestEntitySetter.setSpinEvent(spin, event);
        TestEntitySetter.setSpinLocation(spin, location);
        event.addSpinHistory(spin);
        location.addSpinHistory(spin);

        Map<String, EventStatisticsExtended.LocationStats> stats = 
            EventStatisticsExtended.analyzeLocations(event);
        
        EventStatisticsExtended.LocationStats locationStats = stats.get(location.getName());
        assertThat(locationStats.getHourlyActivity()).isEmpty();
    }

    @Test
    void analyzeRewards_ShouldHandleDuplicateSpins() {
        event.addReward(reward);
        TestEntitySetter.setRewardEvent(reward, event);

        LocalDateTime time = LocalDateTime.now();
        // Add duplicate spins at exact same time
        for (int i = 0; i < 3; i++) {
            SpinHistory spin = TestDataBuilder.aSpinHistory()
                .spinTime(time)
                .isWin(true)
                .build();
            TestEntitySetter.setSpinEvent(spin, event);
            TestEntitySetter.setSpinReward(spin, reward);
            event.addSpinHistory(spin);
        }

        Map<String, EventStatisticsExtended.RewardStats> stats = 
            EventStatisticsExtended.analyzeRewards(event);
        
        EventStatisticsExtended.RewardStats rewardStats = stats.get(reward.getName());
        assertThat(rewardStats.getTotalWins()).isEqualTo(3);
        assertThat(rewardStats.getAvgTimeGap()).isZero(); // All spins at same time
    }

    @Test
    void analyzeParticipants_ShouldHandleMaxValues() {
        event.addParticipant(participant);
        event.addLocation(location);
        TestEntitySetter.setLocationEvent(location, event);

        // Add large number of spins to test numeric overflow
        for (int i = 0; i < 1000; i++) {
            SpinHistory spin = TestDataBuilder.aSpinHistory()
                .spinTime(LocalDateTime.now().plusMinutes(i))
                .isWin(true)
                .build();
            TestEntitySetter.setSpinEvent(spin, event);
            TestEntitySetter.setSpinLocation(spin, location);
            TestEntitySetter.setSpinParticipant(spin, participant);
            event.addSpinHistory(spin);
        }

        Map<String, EventStatisticsExtended.ParticipantStats> stats = 
            EventStatisticsExtended.analyzeParticipants(event);
        
        EventStatisticsExtended.ParticipantStats participantStats = 
            stats.get(participant.getCustomerId());
        assertThat(participantStats.getTotalSpins()).isEqualTo(1000);
        assertThat(participantStats.getWinRate()).isEqualTo(1.0);
    }
}
