package vn.com.fecredit.app.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.entity.base.BaseEntityTest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventTest extends BaseEntityTest {

    private Event event;
    private Region region1;
    private Region region2;
    private Province province1;
    private Province province2;
    private Province province3;

    @BeforeEach
    void setUp() {
        // Create provinces
        province1 = new Province();
        province1.setName("Province 1");
        province1.setCode(generateUniqueCode());
        province1.setStatus(1);

        province2 = new Province();
        province2.setName("Province 2");
        province2.setCode(generateUniqueCode());
        province2.setStatus(1);

        province3 = new Province();
        province3.setName("Province 3");
        province3.setCode(generateUniqueCode());
        province3.setStatus(1);

        // Create regions with different province sets
        region1 = new Region();
        region1.setName("Region 1");
        region1.setCode(generateUniqueCode());
        region1.setStatus(1);
        Set<Province> provinces1 = new HashSet<>();
        provinces1.add(province1);
        provinces1.add(province2);
        region1.setProvinces(provinces1);

        region2 = new Region();
        region2.setName("Region 2");
        region2.setCode(generateUniqueCode());
        region2.setStatus(1);
        Set<Province> provinces2 = new HashSet<>();
        provinces2.add(province2);
        provinces2.add(province3);
        region2.setProvinces(provinces2);

        // Create event
        event = new Event();
        event.setName("Test Event");
        event.setCode(generateUniqueCode());
        event.setInitialSpins(10);
        event.setDailySpinLimit(5);
        event.setStatus(1);
        event.setStartTime(LocalDateTime.now().minusDays(1));
        event.setEndTime(LocalDateTime.now().plusDays(1));
    }

    @Test
    void addLocation_ShouldAllowNonOverlappingProvinces() {
        // Create locations with non-overlapping regions
        EventLocation location1 = new EventLocation();
        location1.setName("Location 1");
        location1.setCode(generateUniqueCode());
        location1.setRegion(new Region());  // Region with no provinces
        location1.setStatus(1);

        EventLocation location2 = new EventLocation();
        location2.setName("Location 2");
        location2.setCode(generateUniqueCode());
        location2.setRegion(new Region());  // Region with no provinces
        location2.setStatus(1);

        // Should be able to add both locations
        assertDoesNotThrow(() -> {
            event.addLocation(location1);
            event.addLocation(location2);
        });
    }

    @Test
    void addLocation_ShouldPreventOverlappingProvinces() {
        // Create locations with overlapping regions
        EventLocation location1 = new EventLocation();
        location1.setName("Location 1");
        location1.setCode(generateUniqueCode());
        location1.setRegion(region1);  // Contains provinces 1,2
        location1.setStatus(1);

        EventLocation location2 = new EventLocation();
        location2.setName("Location 2");
        location2.setCode(generateUniqueCode());
        location2.setRegion(region2);  // Contains provinces 2,3
        location2.setStatus(1);

        // First location should be added successfully
        assertDoesNotThrow(() -> event.addLocation(location1));

        // Second location should throw exception due to overlapping province2
        assertThrows(IllegalArgumentException.class, () -> event.addLocation(location2));
    }

    @Test
    void addLocation_ShouldHandleNullValues() {
        assertDoesNotThrow(() -> event.addLocation(null));

        EventLocation location = new EventLocation();
        location.setName("Location 1");
        location.setCode(generateUniqueCode());
        location.setStatus(1);
        // No region set (null)

        assertDoesNotThrow(() -> event.addLocation(location));
    }

    @Test
    void hasOverlappingProvinces_ShouldHandleEdgeCases() {
        EventLocation location1 = new EventLocation();
        location1.setName("Location 1");
        location1.setCode(generateUniqueCode());
        location1.setRegion(region1);
        location1.setStatus(1);
        event.addLocation(location1);

        // Test with null location
        assertFalse(event.hasOverlappingProvinces(null));

        // Test with location having null region
        EventLocation location2 = new EventLocation();
        location2.setName("Location 2");
        location2.setCode(generateUniqueCode());
        location2.setStatus(1);
        assertFalse(event.hasOverlappingProvinces(location2));
    }

    @Test
    void removeLocation_ShouldWorkCorrectly() {
        EventLocation location = new EventLocation();
        location.setName("Location 1");
        location.setCode(generateUniqueCode());
        location.setRegion(region1);
        location.setStatus(1);

        event.addLocation(location);
        assertTrue(event.getLocations().contains(location));

        event.removeLocation(location);
        assertFalse(event.getLocations().contains(location));
        assertNull(location.getEvent());
    }
}
