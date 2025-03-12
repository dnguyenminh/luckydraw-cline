package vn.com.fecredit.app.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import vn.com.fecredit.app.enums.EventStatus;
import vn.com.fecredit.app.enums.LocationType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventLocationDTO Tests")
class EventLocationDTOTest {

    private EventLocationDTO dto;
    private final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    @BeforeEach
    void setUp() {
        dto = new EventLocationDTO();
        dto.setId(1L);
        dto.setName("Test Location");
        dto.setLatitude(10.0);
        dto.setLongitude(106.0);
        dto.setType(LocationType.STORE);
        dto.setStatus(EventStatus.DRAFT);
        dto.setDailySpinLimit(100);
        dto.setWinProbabilityMultiplier(0.5);
        dto.setCreatedAt(now);
        dto.setMetadata(new ConcurrentHashMap<>());
    }

    @Test
    @DisplayName("Should validate basic properties")
    void shouldValidateBasicProperties() {
        assertAll("Basic properties",
            () -> assertEquals(1L, dto.getId(), "ID should match"),
            () -> assertEquals("Test Location", dto.getName(), "Name should match"),
            () -> assertEquals(10.0, dto.getLatitude(), "Latitude should match"),
            () -> assertEquals(106.0, dto.getLongitude(), "Longitude should match"),
            () -> assertEquals(LocationType.STORE, dto.getType(), "Type should match"),
            () -> assertEquals(EventStatus.DRAFT, dto.getStatus(), "Status should match"),
            () -> assertEquals(100, dto.getDailySpinLimit(), "Spin limit should match"),
            () -> assertEquals(0.5, dto.getWinProbabilityMultiplier(), "Win probability should match")
        );
    }

    @Test
    @DisplayName("Should handle metadata operations safely")
    void shouldHandleMetadataOperationsSafely() {
        assertAll("Metadata operations",
            () -> {
                dto.getMetadata().put("key1", "value1");
                assertEquals("value1", dto.getMetadata().get("key1"),
                    "Should store and retrieve metadata value");
            },
            () -> {
                dto.getMetadata().put("key2", null);
                assertNull(dto.getMetadata().get("key2"),
                    "Should handle null metadata value");
            },
            () -> {
                Map<String, String> newMetadata = new HashMap<>();
                newMetadata.put("key3", "value3");
                dto.setMetadata(newMetadata);
                assertEquals("value3", dto.getMetadata().get("key3"),
                    "Should handle metadata replacement");
            },
            () -> {
                dto.setMetadata(null);
                assertNull(dto.getMetadata(),
                    "Should handle null metadata map");
            }
        );
    }

    @Test
    @DisplayName("Should validate date handling")
    void shouldValidateDateHandling() {
        LocalDateTime created = now;
        LocalDateTime updated = now.plusHours(1);

        assertAll("Date handling",
            () -> {
                dto.setCreatedAt(created);
                dto.setUpdatedAt(updated);
                assertTrue(dto.getUpdatedAt().isAfter(dto.getCreatedAt()),
                    "Updated time should be after created time");
            },
            () -> {
                assertDoesNotThrow(() -> dto.setUpdatedAt(null),
                    "Should allow null updated time");
                assertNull(dto.getUpdatedAt(),
                    "Updated time should be null when set to null");
            },
            () -> {
                assertDoesNotThrow(() -> dto.setCreatedAt(null),
                    "Should allow null created time");
                assertNull(dto.getCreatedAt(),
                    "Created time should be null when set to null");
            }
        );
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        EventLocationDTO same = new EventLocationDTO();
        same.setId(1L);
        same.setName("Test Location");
        same.setLatitude(10.0);
        same.setLongitude(106.0);
        same.setType(LocationType.STORE);
        same.setCreatedAt(now);

        EventLocationDTO different = new EventLocationDTO();
        different.setId(2L);
        different.setName("Different Location");

        assertAll("Equals and hashCode",
            () -> assertEquals(dto, dto, "Object should equal itself"),
            () -> assertEquals(dto, same, "Objects with same ID should be equal"),
            () -> assertEquals(dto.hashCode(), same.hashCode(), "Equal objects should have same hashCode"),
            () -> assertNotEquals(dto, different, "Objects with different IDs should not be equal"),
            () -> assertNotEquals(dto.hashCode(), different.hashCode(), "Different objects should have different hashCodes"),
            () -> assertNotEquals(dto, null, "Object should not equal null"),
            () -> assertNotEquals(dto, new Object(), "Object should not equal different type")
        );
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        String toString = dto.toString();

        assertAll("ToString implementation",
            () -> assertTrue(toString.contains(dto.getId().toString()), "Should contain ID"),
            () -> assertTrue(toString.contains(dto.getName()), "Should contain name"),
            () -> assertTrue(toString.contains(dto.getType().toString()), "Should contain type"),
            () -> assertTrue(toString.contains(String.valueOf(dto.getLatitude())), "Should contain latitude"),
            () -> assertTrue(toString.contains(String.valueOf(dto.getLongitude())), "Should contain longitude")
        );
    }

    @ParameterizedTest
    @CsvSource({
        "-91.0, false",
        "-90.0, true",
        "0.0, true",
        "90.0, true",
        "91.0, false"
    })
    @DisplayName("Should validate coordinates")
    void shouldValidateCoordinates(double value, boolean expectedValid) {
        dto.setLatitude(value);
        assertEquals(expectedValid, isValidCoordinates(dto),
            String.format("Latitude %f validation should be %s", value, expectedValid));

        dto.setLatitude(10.0);
        dto.setLongitude(value * 2);
        assertEquals(expectedValid, isValidCoordinates(dto),
            String.format("Longitude %f validation should be %s", value * 2, expectedValid));
    }

    @Test
    @DisplayName("Should validate status transitions")
    void shouldValidateStatusTransitions() {
        assertAll("Status transitions",
            () -> {
                dto.setStatus(EventStatus.DRAFT);
                dto.setDailySpinLimit(0);
                assertTrue(isValidState(dto), "Should allow zero spin limit in DRAFT status");
            },
            () -> {
                dto.setStatus(EventStatus.ACTIVE);
                assertFalse(isValidState(dto), "Should reject zero spin limit in ACTIVE status");
            },
            () -> {
                dto.setDailySpinLimit(100);
                assertTrue(isValidState(dto), "Should accept valid spin limit in ACTIVE status");
            }
        );
    }

    @Test
    @DisplayName("Should validate win probability")
    void shouldValidateWinProbability() {
        assertAll("Win probability validation",
            () -> {
                dto.setWinProbabilityMultiplier(0.0);
                assertTrue(isValidWinProbability(dto.getWinProbabilityMultiplier()),
                    "Should accept 0.0 probability");
            },
            () -> {
                dto.setWinProbabilityMultiplier(1.0);
                assertTrue(isValidWinProbability(dto.getWinProbabilityMultiplier()),
                    "Should accept 1.0 probability");
            },
            () -> {
                dto.setWinProbabilityMultiplier(-0.1);
                assertFalse(isValidWinProbability(dto.getWinProbabilityMultiplier()),
                    "Should reject negative probability");
            },
            () -> {
                dto.setWinProbabilityMultiplier(1.1);
                assertFalse(isValidWinProbability(dto.getWinProbabilityMultiplier()),
                    "Should reject probability > 1");
            }
        );
    }

    private boolean isValidCoordinates(EventLocationDTO location) {
        return isValidLatitude(location.getLatitude()) && isValidLongitude(location.getLongitude());
    }

    private boolean isValidLatitude(Double latitude) {
        return latitude != null && latitude >= -90.0 && latitude <= 90.0;
    }

    private boolean isValidLongitude(Double longitude) {
        return longitude != null && longitude >= -180.0 && longitude <= 180.0;
    }

    private boolean isValidWinProbability(double probability) {
        return probability >= 0.0 && probability <= 1.0;
    }

    private boolean isValidState(EventLocationDTO location) {
        if (location.getStatus() == EventStatus.ACTIVE) {
            return location.getDailySpinLimit() > 0 && 
                   isValidCoordinates(location) &&
                   isValidWinProbability(location.getWinProbabilityMultiplier());
        }
        return true;
    }
}
