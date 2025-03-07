package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.base.BaseEntityTest;
import java.util.HashSet;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest extends BaseEntityTest {

    private Participant participant;
    private Event event;
    private Province province;
    private Region region;
    private EventLocation eventLocation;

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
        event.addLocation(eventLocation);
    }

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
        event2.addLocation(eventLocation2);

        return event2;
    }

    @Test
    void joinEvent_ShouldSetupInitialValues() {
        ParticipantEvent pe = participant.joinEvent(event, event.getInitialSpins());
        
        assertNotNull(pe);
        assertEquals(event.getInitialSpins(), pe.getAvailableSpins());
        assertEquals(0, pe.getTotalSpins());
        assertEquals(0, pe.getDailySpinCount());
        assertSame(event, pe.getEvent());
    }

    @Test
    void spinCount_ShouldBeTrackedPerEvent() {
        ParticipantEvent pe = participant.joinEvent(event, event.getInitialSpins());
        participant.incrementSpinCount(event);
        
        assertEquals(1, participant.getDailySpinCount(event));
        assertEquals(1, participant.getTotalSpins(event));
        assertEquals(event.getInitialSpins() - 1, pe.getAvailableSpins());
    }

    @Test
    void winCount_ShouldBeTrackedPerEvent() {
        participant.joinEvent(event, event.getInitialSpins());
        participant.incrementWinCount(event);
        
        assertEquals(1, participant.getTotalWins(event));
    }

    @Test
    void points_ShouldBeTrackedPerEvent() {
        participant.joinEvent(event, event.getInitialSpins());
        participant.addPoints(event, 100);
        
        assertEquals(100, participant.getTotalPoints(event));
    }

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
        pe.setAvailableSpins(0);
        assertFalse(participant.canSpin(event));
    }

    @Test
    void leaveEvent_ShouldRemoveParticipation() {
        participant.joinEvent(event, event.getInitialSpins());
        participant.leaveEvent(event);

        assertNull(participant.getEventParticipation(event));
    }

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

    @Test
    void setProvince_ShouldEstablishBidirectionalRelationship() {
        participant.setProvince(province);
        
        assertEquals(province, participant.getProvince());
        assertTrue(province.getParticipants().contains(participant));
    }

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

    @Test
    void setProvince_ToNull_ShouldRemoveFromPreviousProvince() {
        participant.setProvince(province);
        assertTrue(province.getParticipants().contains(participant));
        
        participant.setProvince(null);
        assertNull(participant.getProvince());
        assertFalse(province.getParticipants().contains(participant));
    }
}
