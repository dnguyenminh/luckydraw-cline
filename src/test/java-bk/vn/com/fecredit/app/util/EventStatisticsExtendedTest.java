package vn.com.fecredit.app.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import vn.com.fecredit.app.entity.*;

class EventStatisticsExtendedTest {
    private Event event;
    private EventLocation location1;
    private EventLocation location2;
    private Reward reward1;
    private Reward reward2;
    private Participant participant1;
    private Participant participant2;

    @BeforeEach
    void setUp() {
        TestDataBuilder.resetCounter();
        event = TestDataBuilder.anEvent()
            .name("Extended Test Event")
            .startDate(LocalDateTime.now().minusDays(1))
            .endDate(LocalDateTime.now().plusDays(6))
            .isActive(true)
            .build();

        location1 = TestDataBuilder.anEventLocation()
            .name("Prime Location")
            .winProbabilityMultiplier(1.5)
            .build();

        location2 = TestDataBuilder.anEventLocation()
            .name("Secondary Location")
            .winProbabilityMultiplier(1.2)
            .build();

        reward1 = TestDataBuilder.aReward()
            .name("Grand Prize")
            .winProbability(0.1)
            .quantity(10)
            .dailyLimit(2)
            .build();

        reward2 = TestDataBuilder.aReward()
            .name("Consolation Prize")
            .winProbability(0.3)
            .quantity(100)
            .dailyLimit(20)
            .build();

        participant1 = TestDataBuilder.aParticipant()
            .customerId("CUST001")
            .build();

        participant2 = TestDataBuilder.aParticipant()
            .customerId("CUST002")
            .build();

        event.addLocation(location1);
        event.addLocation(location2);
        event.addReward(reward1);
        event.addReward(reward2);
        event.addParticipant(participant1);
        event.addParticipant(participant2);

        TestEntitySetter.setLocationEvent(location1, event);
        TestEntitySetter.setLocationEvent(location2, event);
        TestEntitySetter.setRewardEvent(reward1, event);
        TestEntitySetter.setRewardEvent(reward2, event);

        addTestSpins();
    }

    private void addTestSpins() {
        LocalDateTime baseTime = event.getStartDate();
        
        // Add morning spins at location1
        addSpin(baseTime.plusHours(9), location1, participant1, reward1, true);
        addSpin(baseTime.plusHours(9), location1, participant1, reward1, true);
        addSpin(baseTime.plusHours(9), location1, participant2, null, false);
        
        // Add afternoon spins at location2
        addSpin(baseTime.plusHours(14), location2, participant1, reward2, true);
        addSpin(baseTime.plusHours(14), location2, participant2, reward2, true);
        addSpin(baseTime.plusHours(14), location2, participant2, null, false);
        
        // Add next day spins
        addSpin(baseTime.plusDays(1).plusHours(10), location1, participant1, reward2, true);
        addSpin(baseTime.plusDays(1).plusHours(15), location2, participant2, reward1, true);
    }

    private void addSpin(LocalDateTime time, EventLocation location, Participant participant, 
                        Reward reward, boolean isWin) {
        SpinHistory spin = TestDataBuilder.aSpinHistory()
            .spinTime(time)
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

    @Test
    void analyzeRewards_ShouldCalculateCorrectStats() {
        Map<String, EventStatisticsExtended.RewardStats> stats = EventStatisticsExtended.analyzeRewards(event);
        
        EventStatisticsExtended.RewardStats grandPrizeStats = stats.get("Grand Prize");
        assertThat(grandPrizeStats.getTotalWins()).isEqualTo(3);
        assertThat(grandPrizeStats.getWinRate()).isCloseTo(0.375, Offset.offset(0.001));
        assertThat(grandPrizeStats.getPeakHours()).contains(9);

        EventStatisticsExtended.RewardStats consolationStats = stats.get("Consolation Prize");
        assertThat(consolationStats.getTotalWins()).isEqualTo(3);
        assertThat(consolationStats.getWinRate()).isCloseTo(0.375, Offset.offset(0.001));
        assertThat(consolationStats.getPeakHours()).contains(14);
    }

    @Test
    void analyzeParticipants_ShouldCalculateCorrectStats() {
        Map<String, EventStatisticsExtended.ParticipantStats> stats = 
            EventStatisticsExtended.analyzeParticipants(event);
        
        EventStatisticsExtended.ParticipantStats participant1Stats = stats.get("CUST001");
        assertThat(participant1Stats.getTotalSpins()).isEqualTo(4);
        assertThat(participant1Stats.getTotalWins()).isEqualTo(3);
        assertThat(participant1Stats.getWinRate()).isCloseTo(0.75, Offset.offset(0.001));
        assertThat(participant1Stats.getUniqueLocationsVisited()).isEqualTo(2);
        assertThat(participant1Stats.getFavoriteLocations()).contains("Prime Location");

        EventStatisticsExtended.ParticipantStats participant2Stats = stats.get("CUST002");
        assertThat(participant2Stats.getTotalSpins()).isEqualTo(4);
        assertThat(participant2Stats.getTotalWins()).isEqualTo(3);
        assertThat(participant2Stats.getFavoriteLocations()).contains("Secondary Location");
    }

    @Test
    void analyzeLocations_ShouldCalculateCorrectStats() {
        Map<String, EventStatisticsExtended.LocationStats> stats = 
            EventStatisticsExtended.analyzeLocations(event);
        
        EventStatisticsExtended.LocationStats location1Stats = stats.get("Prime Location");
        assertThat(location1Stats.getTotalSpins()).isEqualTo(4);
        assertThat(location1Stats.getUniqueParticipants()).isEqualTo(2);
        assertThat(location1Stats.getWinRate()).isCloseTo(0.75, Offset.offset(0.001));
        assertThat(location1Stats.getEffectiveMultiplier()).isEqualTo(1.5);
        assertThat(location1Stats.getTopRewards()).contains("Grand Prize");

        EventStatisticsExtended.LocationStats location2Stats = stats.get("Secondary Location");
        assertThat(location2Stats.getTotalSpins()).isEqualTo(4);
        assertThat(location2Stats.getUniqueParticipants()).isEqualTo(2);
        assertThat(location2Stats.getTopRewards()).contains("Consolation Prize");
    }

    @Test
    void analyzeParticipants_ShouldHandleActiveHours() {
        Map<String, EventStatisticsExtended.ParticipantStats> stats = 
            EventStatisticsExtended.analyzeParticipants(event);
        
        EventStatisticsExtended.ParticipantStats participant1Stats = stats.get("CUST001");
        assertThat(participant1Stats.getActiveHours())
            .containsExactly(9, 10, 14)
            .isSorted();

        EventStatisticsExtended.ParticipantStats participant2Stats = stats.get("CUST002");
        assertThat(participant2Stats.getActiveHours())
            .containsExactly(9, 14, 15)
            .isSorted();
    }

    @Test
    void analyzeRewards_ShouldCalculateTimeGaps() {
        Map<String, EventStatisticsExtended.RewardStats> stats = EventStatisticsExtended.analyzeRewards(event);
        
        EventStatisticsExtended.RewardStats grandPrizeStats = stats.get("Grand Prize");
        assertThat(grandPrizeStats.getAvgTimeGap()).isPositive();
        
        Map<DayOfWeek, Integer> distribution = grandPrizeStats.getDistributionByDay();
        assertThat(distribution).hasSize(2); // Spins across two days
    }

    @Test
    void analyzeLocations_ShouldCalculateHourlyActivity() {
        Map<String, EventStatisticsExtended.LocationStats> stats = 
            EventStatisticsExtended.analyzeLocations(event);
        
        EventStatisticsExtended.LocationStats location1Stats = stats.get("Prime Location");
        Map<Integer, Double> hourlyActivity = location1Stats.getHourlyActivity();
        
        assertThat(hourlyActivity.get(9)).isCloseTo(0.75, Offset.offset(0.01));
        assertThat(hourlyActivity.get(10)).isCloseTo(0.25, Offset.offset(0.01));
    }
}
