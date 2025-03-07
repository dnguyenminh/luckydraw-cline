package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.base.BaseEntityTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantEventTest extends BaseEntityTest {

    private ParticipantEvent participantEvent;
    private Event event;
    private EventLocation eventLocation;
    private Participant participant;
    private Region region;

    @BeforeEach
    void setUp() {
        region = new Region();
        region.setName("Test Region");
        region.setCode(generateUniqueCode());
        region.setStatus(1); // STATUS_ACTIVE

        event = new Event();
        event.setName("Test Event");
        event.setCode(generateUniqueCode());
        event.setInitialSpins(10);
        event.setDailySpinLimit(5);
        event.setStatus(1); // STATUS_ACTIVE
        // Set event time window
        event.setStartTime(LocalDateTime.now().minusDays(1));
        event.setEndTime(LocalDateTime.now().plusDays(1));

        eventLocation = new EventLocation();
        eventLocation.setName("Test Location");
        eventLocation.setCode(generateUniqueCode());
        eventLocation.setStatus(1); // STATUS_ACTIVE
        eventLocation.setEvent(event);
        eventLocation.setRegion(region);
        
        participant = new Participant();
        participant.setName("Test Participant");
        participant.setCode(generateUniqueCode());
        participant.setStatus(1); // STATUS_ACTIVE

        participantEvent = new ParticipantEvent();
        participantEvent.setParticipant(participant);
        participantEvent.setEventLocation(eventLocation);
        participantEvent.setStatus(1); // STATUS_ACTIVE
        participantEvent.setAvailableSpins(event.getInitialSpins());
    }

    @Test
    void initialState_ShouldBeValid() {
        assertEquals(event.getInitialSpins(), participantEvent.getAvailableSpins());
        assertEquals(0, participantEvent.getDailySpinCount());
        assertEquals(0, participantEvent.getTotalSpins());
        assertEquals(0, participantEvent.getTotalWins());
        assertEquals(0, participantEvent.getTotalPoints());
        assertTrue(participantEvent.isActive());
    }

    @Test
    void incrementSpinCount_ShouldUpdateCountersAndAvailableSpins() {
        participantEvent.incrementSpinCount();
        
        assertEquals(1, participantEvent.getDailySpinCount());
        assertEquals(1, participantEvent.getTotalSpins());
        assertEquals(event.getInitialSpins() - 1, participantEvent.getAvailableSpins());
    }

    @Test
    void incrementSpinCount_ShouldNotDecrementWhenNoAvailableSpins() {
        participantEvent.setAvailableSpins(0);
        participantEvent.incrementSpinCount();
        
        assertEquals(0, participantEvent.getDailySpinCount());
        assertEquals(0, participantEvent.getTotalSpins());
        assertEquals(0, participantEvent.getAvailableSpins());
    }

    @Test
    void incrementSpinCount_ShouldWorkWithMaxValues() {
        participantEvent.setTotalSpins(Integer.MAX_VALUE - 1);
        participantEvent.incrementSpinCount();
        assertEquals(Integer.MAX_VALUE, participantEvent.getTotalSpins());
    }

    @Test
    void incrementWinCount_ShouldUpdateCounter() {
        participantEvent.incrementWinCount();
        assertEquals(1, participantEvent.getTotalWins());
        
        participantEvent.incrementWinCount();
        assertEquals(2, participantEvent.getTotalWins());
    }

    @Test
    void incrementWinCount_ShouldWorkWithMaxValues() {
        participantEvent.setTotalWins(Integer.MAX_VALUE - 1);
        participantEvent.incrementWinCount();
        assertEquals(Integer.MAX_VALUE, participantEvent.getTotalWins());
    }

    @Test
    void addPoints_ShouldUpdatePoints() {
        participantEvent.addPoints(100);
        assertEquals(100, participantEvent.getTotalPoints());
        
        participantEvent.addPoints(50);
        assertEquals(150, participantEvent.getTotalPoints());
    }

    @Test
    void addPoints_ShouldHandleNullAndNegativePoints() {
        assertThrows(IllegalArgumentException.class, () -> participantEvent.addPoints(null));
        assertThrows(IllegalArgumentException.class, () -> participantEvent.addPoints(-50));
        assertEquals(0, participantEvent.getTotalPoints());
        
        participantEvent.addPoints(100);
        assertEquals(100, participantEvent.getTotalPoints());
    }

    @Test
    void resetDailySpinCount_ShouldResetCounter() {
        participantEvent.incrementSpinCount();
        participantEvent.incrementSpinCount();
        assertEquals(2, participantEvent.getDailySpinCount());
        
        participantEvent.resetDailySpinCount();
        assertEquals(0, participantEvent.getDailySpinCount());
        
        // Should not affect other counters
        assertEquals(2, participantEvent.getTotalSpins());
        assertEquals(event.getInitialSpins() - 2, participantEvent.getAvailableSpins());
    }

    @Test
    void canSpin_ShouldConsiderAvailableSpinsAndDailyLimit() {
        // Initial state
        assertTrue(participantEvent.canSpin());

        // Use up daily limit
        for (int i = 0; i < event.getDailySpinLimit(); i++) {
            assertTrue(participantEvent.canSpin());
            participantEvent.incrementSpinCount();
        }
        assertFalse(participantEvent.canSpin());

        // Reset daily count but keep available spins
        participantEvent.resetDailySpinCount();
        assertTrue(participantEvent.canSpin());

        // Set available spins to 0
        participantEvent.setAvailableSpins(0);
        assertFalse(participantEvent.canSpin());

        // Add more spins
        participantEvent.addAvailableSpins(5);
        assertTrue(participantEvent.canSpin());
    }

    @Test
    void canSpin_ShouldConsiderEventTimeWindow() {
        assertTrue(participantEvent.canSpin());

        // Set event end time to past
        event.setEndTime(LocalDateTime.now().minusDays(1));
        assertFalse(participantEvent.canSpin());

        // Set event start time to future
        event.setStartTime(LocalDateTime.now().plusDays(1));
        event.setEndTime(LocalDateTime.now().plusDays(2));
        assertFalse(participantEvent.canSpin());

        // Set valid time window
        event.setStartTime(LocalDateTime.now().minusDays(1));
        event.setEndTime(LocalDateTime.now().plusDays(1));
        assertTrue(participantEvent.canSpin());
    }

    @Test
    void addAvailableSpins_ShouldHandleNegativeAndZeroValues() {
        int initialSpins = participantEvent.getAvailableSpins();
        
        participantEvent.addAvailableSpins(0);
        assertEquals(initialSpins, participantEvent.getAvailableSpins());
        
        assertThrows(IllegalArgumentException.class, () -> participantEvent.addAvailableSpins(-5));
        assertEquals(initialSpins, participantEvent.getAvailableSpins());
    }

    @Test
    void relationships_ShouldBeProperlyManaged() {
        assertEquals(event, participantEvent.getEvent());
        assertEquals(participant, participantEvent.getParticipant());
        assertEquals(eventLocation, participantEvent.getEventLocation());
        assertEquals(region, eventLocation.getRegion());
        
        // Test null safety
        participantEvent.setEventLocation(null);
        participantEvent.setParticipant(null);
        assertNull(participantEvent.getEvent());
        assertNull(participantEvent.getParticipant());
        assertNull(participantEvent.getEventLocation());
    }

    @Test
    void isActive_ShouldConsiderAllEntityStatuses() {
        assertTrue(participantEvent.isActive());
        
        participantEvent.setStatus(0); // STATUS_INACTIVE
        assertFalse(participantEvent.isActive());
        
        participantEvent.setStatus(1); // STATUS_ACTIVE
        event.setStatus(0); // STATUS_INACTIVE
        assertFalse(participantEvent.isActive());
        
        event.setStatus(1); // STATUS_ACTIVE
        participant.setStatus(0); // STATUS_INACTIVE
        assertFalse(participantEvent.isActive());
        
        eventLocation.setStatus(0); // STATUS_INACTIVE
        assertFalse(participantEvent.isActive());

        region.setStatus(0); // STATUS_INACTIVE
        assertFalse(participantEvent.isActive());

        // All active
        participant.setStatus(1); // STATUS_ACTIVE
        event.setStatus(1); // STATUS_ACTIVE
        eventLocation.setStatus(1); // STATUS_ACTIVE
        region.setStatus(1); // STATUS_ACTIVE
        participantEvent.setStatus(1); // STATUS_ACTIVE
        assertTrue(participantEvent.isActive());
    }

    @Test
    void isActive_ShouldHandleNullRelationships() {
        participantEvent.setEventLocation(null);
        assertFalse(participantEvent.isActive());
        
        participantEvent.setEventLocation(eventLocation);
        participantEvent.setParticipant(null);
        assertFalse(participantEvent.isActive());
    }
}
