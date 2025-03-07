package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.base.BaseEntityTest;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;

import static org.junit.jupiter.api.Assertions.*;

class SpinHistoryTest extends BaseEntityTest {

    private SpinHistory spinHistory;
    private Event event;
    private Participant participant;
    private Reward reward;
    private Region region;

    @BeforeEach
    void setUp() {
        // Set up region
        region = new Region();
        region.setName("Test Region");
        region.setCode(generateUniqueCode());
        region.setStatus(AbstractStatusAwareEntity.STATUS_ACTIVE);
        
        // Set up event
        event = new Event();
        event.setName("Test Event");
        event.setCode(generateUniqueCode());
        event.setInitialSpins(10);
        event.setDailySpinLimit(5);
        event.setStatus(AbstractStatusAwareEntity.STATUS_ACTIVE);
        event.setStartTime(now.minusDays(1));
        event.setEndTime(now.plusDays(1));

        // Set up participant
        participant = new Participant();
        participant.setName("Test Participant");
        participant.setCode(generateUniqueCode());
        participant.setStatus(AbstractStatusAwareEntity.STATUS_ACTIVE);

        // Set up reward
        reward = new Reward();
        reward.setName("Test Reward");
        reward.setCode(generateUniqueCode());
        reward.setPoints(100);
        reward.setPointsRequired(50);
        reward.setStatus(AbstractStatusAwareEntity.STATUS_ACTIVE);

        // Set up SpinHistory
        spinHistory = new SpinHistory();
        spinHistory.setMetadata(generateMetadata("spin"));
        spinHistory.setStatus(AbstractStatusAwareEntity.STATUS_ACTIVE);

        // Set up event participation
        participant.joinEvent(event, event.getInitialSpins());
    }

    @Test
    void winStatus_ShouldUpdatePoints() {
        spinHistory.setReward(reward);
        spinHistory.setWin(true);

        assertTrue(spinHistory.isWin());
        assertEquals(reward.getPoints(), spinHistory.getPointsEarned());
        assertEquals(reward.getPointsRequired(), spinHistory.getPointsSpent());
    }

    @Test
    void nonWinStatus_ShouldNotUpdatePoints() {
        spinHistory.setReward(reward);
        spinHistory.setWin(false);

        assertFalse(spinHistory.isWin());
        assertEquals(0, spinHistory.getPointsEarned());
    }

    @Test
    void winPoints_ShouldHandleNullReward() {
        spinHistory.setWin(true);
        
        assertEquals(0, spinHistory.getPointsEarned());
        assertEquals(0, spinHistory.getPointsSpent());
    }

    @Test
    void isActive_ShouldConsiderLocationStatus() {
        EventLocation location = new EventLocation();
        location.setName("Test Location");
        location.setCode(generateUniqueCode());
        location.setStatus(AbstractStatusAwareEntity.STATUS_ACTIVE);
        location.setRegion(region);
        event.addLocation(location);
        
        spinHistory.setEventLocation(location);
        spinHistory.setParticipant(participant);
        spinHistory.setStatus(AbstractStatusAwareEntity.STATUS_ACTIVE);

        assertTrue(location.isActive(), "Location should be active");
        assertTrue(spinHistory.isActive(), "SpinHistory should be active");

        location.setStatus(AbstractStatusAwareEntity.STATUS_INACTIVE);
        assertFalse(spinHistory.isActive(), "SpinHistory should be inactive when location is inactive");
    }

    @Test
    void isActive_ShouldConsiderParticipantStatus() {
        EventLocation location = new EventLocation();
        location.setName("Test Location");
        location.setCode(generateUniqueCode());
        location.setStatus(AbstractStatusAwareEntity.STATUS_ACTIVE);
        location.setRegion(region);
        event.addLocation(location);

        spinHistory.setEventLocation(location);
        spinHistory.setParticipant(participant);
        spinHistory.setStatus(AbstractStatusAwareEntity.STATUS_ACTIVE);

        assertTrue(location.isActive(), "Location should be active");
        assertTrue(participant.isActive(), "Participant should be active");
        assertTrue(spinHistory.isActive(), "SpinHistory should be active");

        participant.setStatus(AbstractStatusAwareEntity.STATUS_INACTIVE);
        assertFalse(spinHistory.isActive(), "SpinHistory should be inactive when participant is inactive");
    }

    @Test
    void relationships_ShouldBeProperlyManaged() {
        EventLocation location = new EventLocation();
        location.setName("Test Location");
        location.setCode(generateUniqueCode());
        location.setStatus(AbstractStatusAwareEntity.STATUS_ACTIVE);
        location.setRegion(region);
        event.addLocation(location);

        spinHistory.setEventLocation(location);
        spinHistory.setParticipant(participant);
        spinHistory.setReward(reward);

        assertEquals(location, spinHistory.getEventLocation());
        assertEquals(event, spinHistory.getEventLocation().getEvent());
        assertEquals(participant, spinHistory.getParticipant());
        assertEquals(reward, spinHistory.getReward());
    }

    @Test
    void toString_ShouldIncludeKeyFields() {
        spinHistory.setParticipant(participant);
        spinHistory.setReward(reward);
        spinHistory.setWin(true);

        String result = spinHistory.toString();
        assertTrue(result.contains(participant.getCode()), "Should contain participant code");
        assertTrue(result.contains(reward.getCode()), "Should contain reward code");
        assertTrue(result.contains("true"), "Should contain win status");
    }
}
