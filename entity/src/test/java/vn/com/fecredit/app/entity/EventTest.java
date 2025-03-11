package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventTest {

    private Event event;
    private Region region1;
    private Region region2;
    private Province province1;
    private Province province2;
    private EventLocation location1;
    private EventLocation location2;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        event = Event.builder()
            .name("Test Event")
            .code("TEST_EVENT")
            .status(1)
            .startTime(now)
            .endTime(now.plusDays(7))
            .defaultWinProbability(0.1)
            .dailySpinLimit(5)
            .initialSpins(10)
            .build();

        region1 = Region.builder()
            .name("Region 1")
            .code("REG1")
            .status(1)
            .build();

        region2 = Region.builder()
            .name("Region 2")
            .code("REG2")
            .status(1)
            .build();

        province1 = Province.builder()
            .name("Province 1")
            .code("PROV1")
            .status(1)
            .build();

        province2 = Province.builder()
            .name("Province 2")
            .code("PROV2")
            .status(1)
            .build();

        location1 = EventLocation.builder()
            .name("Location 1")
            .code("LOC1")
            .status(1)
            .build();

        location2 = EventLocation.builder()
            .name("Location 2")
            .code("LOC2")
            .status(1)
            .build();
    }

    @Test
    void testEventLocationAssociation() {
        region1.addProvince(province1);
        region2.addProvince(province2);

        location1.setRegion(region1);
        location2.setRegion(region2);

        // Add locations to event
        event.addLocation(location1);
        event.addLocation(location2);

        assertEquals(2, event.getLocations().size());
        assertTrue(event.getLocations().contains(location1));
        assertTrue(event.getLocations().contains(location2));
        assertEquals(event, location1.getEvent());
        assertEquals(event, location2.getEvent());

        // Test removal
        event.removeLocation(location1);
        assertEquals(1, event.getLocations().size());
        assertFalse(event.getLocations().contains(location1));
        assertNull(location1.getEvent());
    }

    @Test
    void testOverlappingProvinces() {
        // Setup regions with overlapping province
        region1.addProvince(province1);
        region2.addProvince(province1); 

        location1.setRegion(region1);
        location2.setRegion(region2);

        event.addLocation(location1);
        
        // Should throw exception when adding location with overlapping province
        assertThrows(IllegalArgumentException.class, () -> {
            event.addLocation(location2);
        });
    }

    @Test
    void testEventTimeBoundaryActivation() {
        LocalDateTime now = LocalDateTime.now();
        
        // Test current active event
        event.setStartTime(now.minusDays(1));
        event.setEndTime(now.plusDays(1));
        assertTrue(event.isActive());

        // Test past event
        event.setStartTime(now.minusDays(2));
        event.setEndTime(now.minusDays(1));
        assertFalse(event.isActive());

        // Test future event
        event.setStartTime(now.plusDays(1));
        event.setEndTime(now.plusDays(2));
        assertFalse(event.isActive());

        // Test null dates
        event.setStartTime(null);
        event.setEndTime(null);
        assertFalse(event.isActive());
    }

    @Test
    void testDefaultLocation() {
        assertNull(event.getDefaultLocation());

        location1.setRegion(region1);
        event.addLocation(location1);
        assertEquals(location1, event.getDefaultLocation());

        location2.setRegion(region2);
        event.addLocation(location2);
        // Default location should still be the first one added
        assertEquals(location1, event.getDefaultLocation());

        event.removeLocation(location1);
        assertEquals(location2, event.getDefaultLocation());
    }

    @Test
    void testNullLocationHandling() {
        assertDoesNotThrow(() -> event.addLocation(null));
        assertDoesNotThrow(() -> event.removeLocation(null));
        assertTrue(event.getLocations().isEmpty());
    }

    @Test
    void testOverlappingProvincesWithNull() {
        location1.setRegion(null);
        location2.setRegion(region2);

        event.addLocation(location1);
        // Should not throw exception when region is null
        assertDoesNotThrow(() -> event.addLocation(location2));
    }
}
