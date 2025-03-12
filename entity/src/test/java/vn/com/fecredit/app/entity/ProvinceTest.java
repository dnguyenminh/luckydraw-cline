package vn.com.fecredit.app.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProvinceTest {

    private Province province;
    private Region region;

    @BeforeEach
    void setUp() {
        region = Region.builder()
            .name("Test Region")
            .code("TEST_REG")
            .status(1)
            .build();

        province = Province.builder()
            .name("Test Province")
            .code("TEST_PROV")
            .status(1)
            .region(region)
            .build();
    }

    @Test
    void testActivationRules() {
        // Test activation with inactive region
        region.setStatus(0);
        assertThrows(IllegalStateException.class, () -> {
            province.activate();
        });

        // Test successful activation
        region.setStatus(1);
        assertDoesNotThrow(() -> {
            province.activate();
        });
        assertTrue(province.isActive());
    }

    @Test
    void testDeactivation() {
        // Test deactivation
        province.setStatus(1);
        assertTrue(province.isActive());

        province.deactivate();
        assertFalse(province.isActive());
    }

    @Test
    void testStateValidation() {
        // Test code normalization
        province.setCode("test_prov");
        province.validateState();
        assertEquals("TEST_PROV", province.getCode());

        // Test null name
        Province invalidProvince = Province.builder()
            .code("TEST")
            .region(region)
            .build();
        assertThrows(IllegalStateException.class, () -> invalidProvince.validateState());

        // Test empty name
        invalidProvince.setName("");
        assertThrows(IllegalStateException.class, () -> invalidProvince.validateState());

        // Test null code
        invalidProvince.setName("Test");
        invalidProvince.setCode(null);
        assertThrows(IllegalStateException.class, () -> invalidProvince.validateState());

        // Test empty code
        invalidProvince.setCode("");
        assertThrows(IllegalStateException.class, () -> invalidProvince.validateState());

        // Test null region
        invalidProvince.setCode("TEST");
        invalidProvince.setRegion(null);
        assertThrows(IllegalStateException.class, () -> invalidProvince.validateState());
    }

    @Test
    void testToString() {
        province.setId(1L);
        String expected = "Province[id=1, code=TEST_PROV, name=Test Province]";
        assertEquals(expected, province.toString());
    }
}
