package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for the EventLocation entity.
 * This class tests the functionality of the EventLocation entity, including:
 * - Value inheritance and override mechanisms
 * - Activation and deactivation rules
 * - State validation
 * - Collection initialization
 * - Relationship management with other entities
 */
class EventLocationTest {

    private EventLocation location;
    private Event event;
    private Region region;

    /**
     * Sets up the test environment before each test.
     * Creates and configures all necessary entities for testing EventLocation:
     * - Event with time boundaries and default values
     * - Region with default win probability
     * - EventLocation linked to both event and region
     */
    @BeforeEach
    void setUp() {
        event = Event.builder()
            .name("Test Event")
            .code("TEST_EVENT")
            .status(1)
            .startTime(LocalDateTime.now())
            .endTime(LocalDateTime.now().plusDays(7))
            .defaultWinProbability(0.1)
            .dailySpinLimit(5)
            .initialSpins(10)
            .build();

        region = Region.builder()
            .name("Test Region")
            .code("TEST_REG")
            .status(1)
            .defaultWinProbability(0.2)
            .build();

        location = EventLocation.builder()
            .name("Test Location")
            .code("TEST_LOC")
            .status(1)
            .event(event)
            .region(region)
            .build();
    }

    /**
     * Tests the effective value calculation mechanism of EventLocation.
     * Verifies that:
     * - When location values are null, they inherit from event or region
     * - When location values are set, they override the inherited values
     * - Region values take precedence over event values for win probability
     */
    @Test
    void testEffectiveValues() {
        // Test event defaults
        assertNull(location.getDailySpinLimit());
        assertEquals(5, location.getEffectiveDailySpinLimit());
        
        assertNull(location.getInitialSpins());
        assertEquals(10, location.getEffectiveInitialSpins());
        
        assertNull(location.getDefaultWinProbability());
        assertEquals(0.2, location.getEffectiveWinProbability()); // Should use region's value

        // Test location overrides
        location.setDailySpinLimit(3);
        location.setInitialSpins(7);
        location.setDefaultWinProbability(0.3);

        assertEquals(3, location.getEffectiveDailySpinLimit());
        assertEquals(7, location.getEffectiveInitialSpins());
        assertEquals(0.3, location.getEffectiveWinProbability());
    }

    /**
     * Tests the activation rules for EventLocation.
     * Verifies that:
     * - Location cannot be activated if its event is inactive
     * - Location cannot be activated if its region is inactive
     * - Location can be activated when both event and region are active
     */
    @Test
    void testActivationRules() {
        // Test activation with inactive event
        event.setStatus(0);
        assertThrows(IllegalStateException.class, () -> location.activate());
        event.setStatus(1);

        // Test activation with inactive region
        region.setStatus(0);
        assertThrows(IllegalStateException.class, () -> location.activate());
        region.setStatus(1);

        // Test successful activation
        assertDoesNotThrow(() -> location.activate());
        assertTrue(location.isActive());
    }

    /**
     * Tests the deactivation rules for EventLocation.
     * Verifies that:
     * - Location cannot be deactivated if it has active participants
     * - Location can be deactivated when it has no active participants
     */
    @Test
    void testDeactivationWithActiveParticipants() {
        ParticipantEvent participantEvent = ParticipantEvent.builder()
            .status(1)
            .eventLocation(location)
            .build();
        location.getParticipantEvents().add(participantEvent);

        // Test deactivation with active participants
        assertThrows(IllegalStateException.class, () -> location.deactivate());

        // Test successful deactivation
        location.getParticipantEvents().clear();
        assertDoesNotThrow(() -> location.deactivate());
        assertFalse(location.isActive());
    }

    /**
     * Tests the state validation rules for EventLocation.
     * Verifies that:
     * - Code is normalized to uppercase
     * - Invalid numeric values throw exceptions
     * - Required relationships must be present
     */
    @Test
    void testStateValidation() {
        // Test code normalization
        location.setCode("test_loc");
        location.validateState();
        assertEquals("TEST_LOC", location.getCode());

        // Test invalid values
        location.setDailySpinLimit(-1);
        assertThrows(IllegalStateException.class, () -> location.validateState());

        location.setDailySpinLimit(5);
        location.setInitialSpins(-1);
        assertThrows(IllegalStateException.class, () -> location.validateState());

        location.setInitialSpins(5);
        location.setDefaultWinProbability(-0.1);
        assertThrows(IllegalStateException.class, () -> location.validateState());

        location.setDefaultWinProbability(1.1);
        assertThrows(IllegalStateException.class, () -> location.validateState());

        // Test null required relationships
        EventLocation invalidLocation = EventLocation.builder()
            .name("Invalid")
            .code("INVALID")
            .build();
        assertThrows(IllegalStateException.class, () -> invalidLocation.validateState());

        invalidLocation.setEvent(event);
        assertThrows(IllegalStateException.class, () -> invalidLocation.validateState());
    }

    /**
     * Tests that collections are properly initialized in a new EventLocation.
     * Verifies that all collection properties are non-null, even for a newly created instance.
     */
    @Test
    void testCollectionInitialization() {
        EventLocation newLocation = new EventLocation();
        assertNotNull(newLocation.getParticipantEvents());
        assertNotNull(newLocation.getRewards());
        assertNotNull(newLocation.getGoldenHours());
        assertNotNull(newLocation.getSpinHistories());
    }
}
