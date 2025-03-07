package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GoldenHourTest {

    private static final int STATUS_INACTIVE = 0;
    private static final int STATUS_ACTIVE = 1;

    private GoldenHour goldenHour;
    private Event event;
    private EventLocation location;
    private LocalDateTime now;
    private Region region;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        // Create event
        event = Event.builder()
                .status(STATUS_ACTIVE)
                .build();
                
        // Create region
        region = Region.builder()
                .code("TEST_REGION")
                .name("Test Region")
                .status(STATUS_ACTIVE)
                .build();
                
        // Create location with required properties
        location = EventLocation.builder()
                .event(event)
                .region(region)
                .code("TEST_LOC")
                .status(STATUS_ACTIVE)
                .build();
                
        // Create golden hour
        goldenHour = new GoldenHour();
        
        // Setup relationships
        event.addLocation(location);
        
        // Setup basic properties
        goldenHour.setPointsMultiplier(2.0);
        goldenHour.setWinProbabilityMultiplier(1.5);
        goldenHour.setStatus(STATUS_INACTIVE);
        goldenHour.setEventLocation(location);
        goldenHour.setStartTime(now.minusHours(1));
        goldenHour.setEndTime(now.plusHours(1));
    }

    @Test
    void testInitialStatus() {
        GoldenHour newGoldenHour = new GoldenHour();
        assertEquals(STATUS_INACTIVE, newGoldenHour.getStatus());
    }

    @Test
    void testStatusTransitions() {
        goldenHour.setStatus(STATUS_ACTIVE);
        assertEquals(STATUS_ACTIVE, goldenHour.getStatus());
        
        goldenHour.setStatus(STATUS_INACTIVE);
        assertEquals(STATUS_INACTIVE, goldenHour.getStatus());
    }

    @Test
    void testMultipliers() {
        assertEquals(2.0, goldenHour.getPointsMultiplier());
        assertEquals(1.5, goldenHour.getWinProbabilityMultiplier());

        // Test default values
        GoldenHour newGoldenHour = new GoldenHour();
        assertEquals(1.0, newGoldenHour.getPointsMultiplier());
        assertEquals(1.0, newGoldenHour.getWinProbabilityMultiplier());
    }

    @Test
    void testEventLocationRelationship() {
        EventLocation newLocation = EventLocation.builder()
                .event(event)
                .region(region)
                .status(STATUS_ACTIVE)
                .build();
        goldenHour.setEventLocation(newLocation);
        
        assertEquals(event, goldenHour.getEventLocation().getEvent());
        assertTrue(newLocation.getGoldenHours().contains(goldenHour));
    }

    @Test
    void testLocationRelationship() {
        EventLocation newLocation = EventLocation.builder()
                .event(event)
                .region(region)
                .status(STATUS_ACTIVE)
                .build();
        goldenHour.setEventLocation(newLocation);
        assertEquals(newLocation, goldenHour.getEventLocation());
        assertTrue(newLocation.getGoldenHours().contains(goldenHour));
        assertFalse(location.getGoldenHours().contains(goldenHour));
    }

    @Test
    void testActiveStatus() {
        goldenHour.setStatus(STATUS_ACTIVE);
        location.setStatus(STATUS_ACTIVE);
        event.setStatus(STATUS_ACTIVE);
        event.setStartTime(now.minusHours(1));
        event.setEndTime(now.plusHours(1));
        assertTrue(goldenHour.isCurrentlyActive());

        // Test various inactive conditions
        goldenHour.setStatus(STATUS_INACTIVE);
        assertFalse(goldenHour.isCurrentlyActive());

        goldenHour.setStatus(STATUS_ACTIVE);
        location.setStatus(STATUS_INACTIVE);
        assertFalse(goldenHour.isCurrentlyActive());
    }

    @Test
    void testTimeBasedActivation() {
        goldenHour.setStatus(STATUS_ACTIVE);
        location.setStatus(STATUS_ACTIVE);
        event.setStatus(STATUS_ACTIVE);
        event.setStartTime(now.minusHours(1));
        event.setEndTime(now.plusHours(1));
        // Test future time window
        goldenHour.setStartTime(now.plusHours(1));
        goldenHour.setEndTime(now.plusHours(2));
        assertFalse(goldenHour.isCurrentlyActive());

        // Test past time window
        goldenHour.setStartTime(now.minusHours(2));
        goldenHour.setEndTime(now.minusHours(1));
        assertFalse(goldenHour.isCurrentlyActive());

        // Test current time window
        goldenHour.setStartTime(now.minusHours(1));
        goldenHour.setEndTime(now.plusHours(1));
        assertTrue(goldenHour.isCurrentlyActive());
    }

    @Test
    void testUsageLimits() {
        goldenHour.setDailyLimit(5);
        assertEquals(0, goldenHour.getTotalUses());
        
        // Test increment
        for (int i = 0; i < 3; i++) {
            goldenHour.incrementUses();
        }
        assertEquals(3, goldenHour.getTotalUses());

        // Test reset
        goldenHour.resetUses();
        assertEquals(0, goldenHour.getTotalUses());

        // Test limit enforcement
        goldenHour.setDailyLimit(2);
        goldenHour.setStatus(STATUS_ACTIVE);
        location.setStatus(STATUS_ACTIVE);
        event.setStatus(STATUS_ACTIVE);
        event.setStartTime(now.minusHours(1));
        event.setEndTime(now.plusHours(1));

        goldenHour.incrementUses();
        assertTrue(goldenHour.isCurrentlyActive());
        
        goldenHour.incrementUses();
        assertFalse(goldenHour.isCurrentlyActive());
    }

    @Test
    void testWinProbability() {
        location.setDefaultWinProbability(0.1);
        goldenHour.setWinProbability(0.2);
        goldenHour.setWinProbabilityMultiplier(1.5);
        
        assertEquals(0.3, goldenHour.getWinProbability(), 0.001);
        
        // Test fallback to location default
        goldenHour.setWinProbability(null);
        assertEquals(0.15, goldenHour.getWinProbability(), 0.001);
    }
}
