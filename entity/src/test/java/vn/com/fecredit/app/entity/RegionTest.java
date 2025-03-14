package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class RegionTest {

    @Test
    void testRegionBuilder() {
        Region region = Region.builder()
            .name("Test Region")
            .code("TEST")
            .defaultWinProbability(0.5)
            .status(1)
            .provinces(new ArrayList<>())
            .eventLocations(new ArrayList<>())
            .build();

        assertNotNull(region);
        assertEquals("Test Region", region.getName());
        assertEquals("TEST", region.getCode());
        assertEquals(0.5, region.getDefaultWinProbability());
        assertEquals(1, region.getStatus());
        assertTrue(region.getProvinces().isEmpty());
        assertTrue(region.getEventLocations().isEmpty());
    }

    @Test
    void testProvinceListManipulation() {
        Region region = Region.builder()
            .name("Test Region")
            .code("TEST")
            .build();

        Province province = Province.builder()
            .name("Test Province")
            .code("TEST_PROV")
            .build();

        region.addProvince(province);
        assertEquals(1, region.getProvinces().size());
        assertEquals(region, province.getRegion());

        region.removeProvince(province);
        assertTrue(region.getProvinces().isEmpty());
        assertNull(province.getRegion());
    }

    @Test
    void testEventLocationListManipulation() {
        Region region = Region.builder()
            .name("Test Region")
            .code("TEST")
            .build();

        EventLocation location = EventLocation.builder()
            .name("Test Location")
            .code("TEST_LOC")
            .build();

        region.addEventLocation(location);
        assertEquals(1, region.getEventLocations().size());
        assertEquals(region, location.getRegion());

        region.removeEventLocation(location);
        assertTrue(region.getEventLocations().isEmpty());
        assertNull(location.getRegion());
    }

    @Test
    void testCodeNormalization() {
        Region region = new Region();
        region.setCode("test");
        assertEquals("TEST", region.getCode());
    }

    @Test
    void testActivationDeactivation() {
        Region region = Region.builder()
            .name("Test Region")
            .code("TEST")
            .build();

        Province activeProvince = Province.builder()
            .name("Active Province")
            .code("ACT_PROV")
            .status(1)
            .build();

        region.addProvince(activeProvince);

        assertThrows(IllegalStateException.class, () -> {
            region.deactivate();
        });

        region.getProvinces().clear();
        region.deactivate();
        assertFalse(region.isActive());

        region.activate();
        assertTrue(region.isActive());
    }
}
