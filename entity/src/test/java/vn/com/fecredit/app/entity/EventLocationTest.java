package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.base.BaseEntityTest;

class EventLocationTest extends BaseEntityTest {

    private EventLocation location;
    private Event event;
    private Region region;
    private Province province;

    @BeforeEach
    void setUp() {
        // Create event with default configuration
        event = Event.builder()
            .name("Test Event")
            .code(generateUniqueCode())
            .initialSpins(10)
            .dailySpinLimit(5)
            .defaultWinProbability(0.1)
            .status(Event.STATUS_ACTIVE)
            .startTime(LocalDateTime.now().minusDays(1))
            .endTime(LocalDateTime.now().plusDays(1))
            .eventLocations(new LinkedHashSet<>())
//            .rewards(new LinkedHashSet<>())
//            .goldenHours(new LinkedHashSet<>())
//            .spinHistories(new LinkedHashSet<>())
            .build();

        // Create region in active state
        region = new Region();
        region.setName("Test Region");
        region.setCode(generateUniqueCode());
        region.setStatus(Region.STATUS_ACTIVE);

        // Create province in active state
        province = new Province();
        province.setName("Test Province");
        province.setCode(generateUniqueCode());
        province.setStatus(Province.STATUS_ACTIVE);

        // Create location with Event linking
        location = new EventLocation();
        location.setName("Test Location");
        location.setCode(generateUniqueCode());
        location.setStatus(EventLocation.STATUS_ACTIVE);
    }

    @Test
    void testBasicProperties() {
        // Reset location to test default values
        location = new EventLocation();
        location.setStatus(EventLocation.STATUS_ACTIVE);
        
        // Test null values before Event assignment
        assertNull(location.getEffectiveInitialSpins());
        assertNull(location.getEffectiveDailySpinLimit());
        assertNull(location.getEffectiveDefaultWinProbability());
        
        // Assign Event and test inherited values
        location.setEvent(event);
        assertEquals(10, location.getEffectiveInitialSpins());
        assertEquals(5, location.getEffectiveDailySpinLimit());
        assertEquals(0.1, location.getEffectiveDefaultWinProbability());

        // Test overriding values
        location.setInitialSpins(20);
        location.setDailySpinLimit(8);
        location.setDefaultWinProbability(0.2);

        assertEquals(20, location.getEffectiveInitialSpins());
        assertEquals(8, location.getEffectiveDailySpinLimit());
        assertEquals(0.2, location.getEffectiveDefaultWinProbability());
    }

    @Test
    void testEventRelationship() {
        // Create new Event with different configuration
        Event newEvent = Event.builder()
            .code(generateUniqueCode())
            .name("Test Event 2")
            .initialSpins(15)
            .dailySpinLimit(7)
            .defaultWinProbability(0.15)
            .startTime(LocalDateTime.now().minusDays(1))
            .endTime(LocalDateTime.now().plusDays(1))
            .status(Event.STATUS_ACTIVE)
            .eventLocations(new LinkedHashSet<>())
//            .rewards(new LinkedHashSet<>())
//            .goldenHours(new LinkedHashSet<>())
//            .spinHistories(new LinkedHashSet<>())
            .build();

        // Reset location configuration and status
        location = new EventLocation();
        location.setName("Test Location");
        location.setCode(generateUniqueCode());
        location.setStatus(EventLocation.STATUS_ACTIVE);
        
        // Set new Event
        location.setEvent(newEvent);

        // Verify relationship
        assertTrue(newEvent.getEventLocations().contains(location));
        assertEquals(newEvent, location.getEvent());

        // Verify configuration inheritance
        assertEquals(15, location.getEffectiveInitialSpins());
        assertEquals(7, location.getEffectiveDailySpinLimit());
        assertEquals(0.15, location.getEffectiveDefaultWinProbability());

        // Test removal
        location.setEvent(null);
        assertFalse(newEvent.getEventLocations().contains(location));
        assertNull(location.getEvent());
    }

    @Test
    void testActiveStatus() {
        location.setEvent(event);
        location.setRegion(region);
        location.activate();
        
        assertTrue(location.isActive(), "Should be active with all conditions met");

        event.deactivate();
        assertFalse(location.isActive(), "Should be inactive with inactive event");

        event.activate();
        region.deactivate();
        assertFalse(location.isActive(), "Should be inactive with inactive region");

        region.activate();
        assertTrue(location.isActive(), "Should be active with all entities active");
    }

    @Test
    void testCollectionsInitialization() {
        EventLocation newLocation = new EventLocation();
        assertNotNull(newLocation.getRewards());
        assertNotNull(newLocation.getGoldenHours());
        assertNotNull(newLocation.getSpinHistories());
        assertTrue(newLocation.getRewards().isEmpty());
        assertTrue(newLocation.getGoldenHours().isEmpty());
        assertTrue(newLocation.getSpinHistories().isEmpty());
    }
}
