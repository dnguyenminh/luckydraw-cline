package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.base.BaseEntityTest;
import java.util.HashSet;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Participant entity.
 * This class tests the functionality of the Participant entity, including:
 * - Event participation management
 * - Spin count tracking across multiple events
 * - Win count and points accumulation
 * - Daily limit enforcement
 * - Province relationship management
 */
class ParticipantTest extends BaseEntityTest {

    private Participant participant;
    private Event event;
    private Province province;
    private Region region;
    private EventLocation eventLocation;

    /**
     * Sets up the test environment before each test.
     * Creates and configures all necessary entities for testing Participant:
     * - Region with basic properties
     * - Province with basic properties
     * - Participant with metadata and roles
     * - Event with time boundaries and spin limits
     * - EventLocation linked to the region and event
     */
    @BeforeEach
    void setUp() {
        // Create first event setup
        region = new Region();
        region.setName("Test Region");
        region.setCode(generateUniqueCode());
        region.setStatus(1);

        province = new Province();
        province.setName("Test Province");
        province.setCode(generateUniqueCode());
        province.setStatus(Province.STATUS_ACTIVE);
        
        participant = new Participant();
        participant.setName("Test Participant");
        participant.setCode(generateUniqueCode());
        participant.setMetadata(generateMetadata("participant"));
        participant.setStatus(Participant.STATUS_ACTIVE);
        participant.setRoles(new HashSet<>());

        event = new Event();
        event.setName("Test Event");
        event.setCode(generateUniqueCode());
        event.setInitialSpins(10);
        event.setDailySpinLimit(5);
        event.setStatus(Event.STATUS_ACTIVE);
        event.setStartTime(LocalDateTime.now().minusDays(1));
        event.setEndTime(LocalDateTime.now().plusDays(1));

        eventLocation = new EventLocation();
        eventLocation.setName("Test Location");
        eventLocation.setCode(generateUniqueCode());
        eventLocation.setStatus(1);
        eventLocation.setInitialSpins(event.getInitialSpins());
        eventLocation.setDailySpinLimit(event.getDailySpinLimit());
        eventLocation.setDefaultWinProbability(event.getDefaultWinProbability());
        eventLocation.setRegion(region);
        eventLocation.setEvent(event);
        event.addLocation(eventLocation);
    }

    /**
     * Helper method to create a second event for testing multi-event scenarios.
     * Creates a complete event setup with:
     * - Event with different spin limits
     * - Region specific to this event
     * - EventLocation linked to the new event and region
     * 
     * @return A fully configured second Event instance
     */
    private Event createSecondEvent() {
        Event event2 = new Event();
        event2.setName("Test Event 2");
        event2.setCode(generateUniqueCode());
        event2.setInitialSpins(5);
        event2.setDailySpinLimit(3);
        event2.setStatus(Event.STATUS_ACTIVE);
        event2.setStartTime(LocalDateTime.now().minusDays(1));
        event2.setEndTime(LocalDateTime.now().plusDays(1));

        Region region2 = new Region();
        region2.setName("Test Region 2");
        region2.setCode(generateUniqueCode());
        region2.setStatus(1);

        EventLocation eventLocation2 = new EventLocation();
        eventLocation2.setName("Test Location 2");
        eventLocation2.setCode(generateUniqueCode());
        eventLocation2.setStatus(1);
        eventLocation2.setInitialSpins(event2.getInitialSpins());
        eventLocation2.setDailySpinLimit(event2.getDailySpinLimit());
        eventLocation2.setDefaultWinProbability(event2.getDefaultWinProbability());
        eventLocation2.setRegion(region2);
        eventLocation2.setEvent(event2);
        event2.addLocation(eventLocation2);

        return event2;
    }

    /**
     * Tests that when a participant joins an event:
     * - A ParticipantEvent is created with the correct initial values
     * - The remaining spins are set to the event's initial spins
     * - The total spins and daily spin count start at zero
     * - The event reference is correctly established
     */
    @Test
    void joinEvent_ShouldSetupInitialValues() {
        ParticipantEvent pe = participant.joinEvent(event, event.getInitialSpins());
        
        assertNotNull(pe);
        assertEquals(event.getInitialSpins(), pe.getRemainingSpins());
        assertEquals(0, pe.getTotalSpins());
        assertEquals(0, pe.getDailySpinCount());
        assertEquals(event, pe.getEventLocation().getEvent());
    }

    /**
     * Tests that spin counts are tracked separately for each event:
     * - Daily spin count is incremented
     * - Total spins are incremented
     * - Remaining spins are decremented
     */
    @Test
    void spinCount_ShouldBeTrackedPerEvent() {
        ParticipantEvent pe = participant.joinEvent(event, event.getInitialSpins());
        participant.incrementSpinCount(event);
        
        assertEquals(1, participant.getDailySpinCount(event));
        assertEquals(1, participant.getTotalSpins(event));
        assertEquals(event.getInitialSpins() - 1, pe.getRemainingSpins());
    }

    /**
     * Tests that win counts are tracked separately for each event.
     * Verifies that incrementing the win count for one event affects only that event.
     */
    @Test
    void winCount_ShouldBeTrackedPerEvent() {
        participant.joinEvent(event, event.getInitialSpins());
        participant.incrementWinCount(event);
        
        assertEquals(1, participant.getTotalWins(event));
    }

    /**
     * Tests that points are tracked separately for each event.
     * Verifies that adding points to one event affects only that event's total.
     */
    @Test
    void points_ShouldBeTrackedPerEvent() {
        participant.joinEvent(event, event.getInitialSpins());
        participant.addPoints(event, 100);
        
        assertEquals(100, participant.getTotalPoints(event));
    }

    /**
     * Tests that resetting the daily spin count affects only the specified event.
     * Verifies that when a participant is in multiple events, resetting one event's
     * daily spin count does not affect the other event.
     */
    @Test
    void resetDailySpinCount_ShouldOnlyAffectSpecificEvent() {
        Event event2 = createSecondEvent();

        ParticipantEvent pe1 = participant.joinEvent(event, event.getInitialSpins());
        ParticipantEvent pe2 = participant.joinEvent(event2, event2.getInitialSpins());

        participant.incrementSpinCount(event);
        participant.incrementSpinCount(event2);

        // Verify initial state
        assertEquals(1, pe1.getDailySpinCount());
        assertEquals(1, pe2.getDailySpinCount());

        participant.resetDailySpinCount(event);
        
        // After reset
        assertEquals(0, participant.getDailySpinCount(event), "First event spin count should be reset");
        assertEquals(1, participant.getDailySpinCount(event2), "Second event spin count should remain unchanged");
    }

    /**
     * Tests the spin eligibility rules for a participant.
     * Verifies that a participant can only spin when:
     * - They have remaining spins available
     * - They have not reached the daily spin limit
     */
    @Test
    void canSpin_ShouldConsiderAvailableSpinsAndDailyLimit() {
        ParticipantEvent pe = participant.joinEvent(event, 3);
        assertTrue(participant.canSpin(event));

        // Use up daily limit
        for (int i = 0; i < event.getDailySpinLimit(); i++) {
            participant.incrementSpinCount(event);
        }
        assertFalse(participant.canSpin(event));

        // Reset daily count but no available spins
        pe.resetDailySpinCount();
        pe.setRemainingSpins(0);
        assertFalse(participant.canSpin(event));
    }

    /**
     * Tests that when a participant leaves an event:
     * - The ParticipantEvent is removed from the participant
     * - The participant can no longer access that event's data
     */
    @Test
    void leaveEvent_ShouldRemoveParticipation() {
        participant.joinEvent(event, event.getInitialSpins());
        participant.leaveEvent(event);

        assertNull(participant.getEventParticipation(event));
    }

    /**
     * Tests that leaving one event does not affect participation in other events.
     * Verifies that when a participant leaves one event, they remain in other events
     * they have joined.
     */
    @Test
    void leaveEvent_ShouldNotAffectOtherEvents() {
        Event event2 = createSecondEvent();
        
        participant.joinEvent(event, event.getInitialSpins());
        ParticipantEvent pe2 = participant.joinEvent(event2, event2.getInitialSpins());

        participant.leaveEvent(event);

        assertNull(participant.getEventParticipation(event));
        assertNotNull(participant.getEventParticipation(event2));
        assertSame(pe2, participant.getEventParticipation(event2));
    }

    /**
     * Tests the bidirectional relationship between Participant and Province.
     * Verifies that setting a province on a participant:
     * - Updates the participant's province reference
     * - Adds the participant to the province's participants collection
     */
    @Test
    void setProvince_ShouldEstablishBidirectionalRelationship() {
        participant.setProvince(province);
        
        assertEquals(province, participant.getProvince());
        assertTrue(province.getParticipants().contains(participant));
    }

    /**
     * Tests that changing a participant's province updates both the old and new province.
     * Verifies that when a participant changes province:
     * - The participant is removed from the old province's collection
     * - The participant is added to the new province's collection
     * - The participant's province reference is updated
     */
    @Test
    void setProvince_ShouldUpdatePreviousProvince() {
        Province oldProvince = new Province();
        oldProvince.setName("Old Province");
        oldProvince.setCode(generateUniqueCode());
        oldProvince.setStatus(Province.STATUS_ACTIVE);
        
        participant.setProvince(oldProvince);
        assertTrue(oldProvince.getParticipants().contains(participant));
        
        participant.setProvince(province);
        assertFalse(oldProvince.getParticipants().contains(participant));
        assertTrue(province.getParticipants().contains(participant));
    }
    /**
     * Tests that setting a participant's province to null removes it from the province.
     * Verifies that when a participant's province is set to null:
     * - The participant's province reference becomes null
     * - The participant is removed from the province's participants collection
     */
    @Test
    void setProvince_ToNull_ShouldRemoveFromPreviousProvince() {
        participant.setProvince(province);
        assertTrue(province.getParticipants().contains(participant));
        
        participant.setProvince(null);
        assertNull(participant.getProvince());
        assertFalse(province.getParticipants().contains(participant));
    }
}
