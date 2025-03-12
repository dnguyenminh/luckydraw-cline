package vn.com.fecredit.app.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class EntityStatusConverterTest {

    private EntityStatusConverter converter;

    @BeforeEach
    void setUp() {
        converter = new EntityStatusConverter();
    }

    @Test
    void convertToDatabaseColumn_WhenStatusIsNull_ShouldReturnActiveCode() {
        String result = converter.convertToDatabaseColumn(null);
        assertEquals(EntityStatus.ACTIVE.getCode(), result);
    }

    @Test
    void convertToDatabaseColumn_WhenStatusIsValid_ShouldReturnCode() {
        String result = converter.convertToDatabaseColumn(EntityStatus.PENDING);
        assertEquals(EntityStatus.PENDING.getCode(), result);
    }

    @Test
    void convertToEntityAttribute_WhenCodeIsNull_ShouldReturnActive() {
        EntityStatus result = converter.convertToEntityAttribute(null);
        assertEquals(EntityStatus.ACTIVE, result);
    }

    @Test
    void convertToEntityAttribute_WhenCodeIsValid_ShouldReturnCorrectStatus() {
        EntityStatus result = converter.convertToEntityAttribute(EntityStatus.DRAFT.getCode());
        assertEquals(EntityStatus.DRAFT, result);
    }

    @Test
    void convertToEntityAttribute_WhenCodeIsInvalid_ShouldReturnActive() {
        EntityStatus result = converter.convertToEntityAttribute("INVALID_CODE");
        assertEquals(EntityStatus.ACTIVE, result);
    }

    @Test
    void parseStatusSafely_WhenStatusIsNull_ShouldReturnActive() {
        EntityStatus result = EntityStatusConverter.parseStatusSafely(null);
        assertEquals(EntityStatus.ACTIVE, result);
    }

    @Test
    void parseStatusSafely_WhenStatusIsEmpty_ShouldReturnActive() {
        EntityStatus result = EntityStatusConverter.parseStatusSafely("");
        assertEquals(EntityStatus.ACTIVE, result);
    }

    @Test
    void parseStatusSafely_WhenStatusIsValid_ShouldReturnCorrectStatus() {
        EntityStatus result = EntityStatusConverter.parseStatusSafely("PENDING");
        assertEquals(EntityStatus.PENDING, result);
    }

    @Test
    void isValidStatus_WhenStatusIsNull_ShouldReturnFalse() {
        assertFalse(EntityStatusConverter.isValidStatus(null));
    }

    @Test
    void isValidStatus_WhenStatusIsEmpty_ShouldReturnFalse() {
        assertFalse(EntityStatusConverter.isValidStatus(""));
    }

    @Test
    void isValidStatus_WhenStatusIsValid_ShouldReturnTrue() {
        assertTrue(EntityStatusConverter.isValidStatus("ACTIVE"));
    }

    @Test
    void isValidStatus_WhenStatusIsInvalid_ShouldReturnFalse() {
        assertFalse(EntityStatusConverter.isValidStatus("INVALID_STATUS"));
    }

    @Test
    void canTransition_FromDraftToActive_ShouldReturnTrue() {
        assertTrue(EntityStatusConverter.canTransition("DRAFT", "ACTIVE"));
    }

    @Test
    void canTransition_FromActiveToDeleted_ShouldReturnFalse() {
        assertFalse(EntityStatusConverter.canTransition("ACTIVE", "DELETED"));
    }

    @Test
    void canTransition_WithInvalidStatus_ShouldReturnFalse() {
        assertFalse(EntityStatusConverter.canTransition("INVALID", "ACTIVE"));
    }

    @Test
    void getDefaultStatus_ShouldReturnActive() {
        assertEquals(EntityStatus.ACTIVE, EntityStatusConverter.getDefaultStatus());
    }
    
    @Test
    void roundTripConversion_ShouldPreserveStatus() {
        for (EntityStatus status : EntityStatus.values()) {
            String dbValue = converter.convertToDatabaseColumn(status);
            EntityStatus reconverted = converter.convertToEntityAttribute(dbValue);
            assertEquals(status, reconverted, "Round trip conversion failed for " + status);
        }
    }
}
