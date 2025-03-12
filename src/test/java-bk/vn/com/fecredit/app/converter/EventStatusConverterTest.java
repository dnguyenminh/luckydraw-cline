package vn.com.fecredit.app.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import vn.com.fecredit.app.enums.EventStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventStatusConverterTest {

    private EventStatusConverter converter;

    @BeforeEach
    void setUp() {
        converter = new EventStatusConverter();
    }

    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void convertToDatabaseColumn_ShouldConvertValidStatus(EventStatus status) {
        String result = converter.convertToDatabaseColumn(status);
        assertThat(result)
            .isNotNull()
            .isEqualTo(status.name());
    }

    @Test
    void convertToDatabaseColumn_ShouldReturnDraftForNull() {
        String result = converter.convertToDatabaseColumn(null);
        assertThat(result)
            .isNotNull()
            .isEqualTo(EventStatus.DRAFT.name());
    }

    @ParameterizedTest
    @EnumSource(EventStatus.class)
    void convertToEntityAttribute_ShouldConvertValidString(EventStatus status) {
        EventStatus result = converter.convertToEntityAttribute(status.name());
        assertThat(result)
            .isNotNull()
            .isEqualTo(status);
    }

    @ParameterizedTest
    @NullSource
    void convertToEntityAttribute_ShouldReturnDraftForNull(String value) {
        EventStatus result = converter.convertToEntityAttribute(value);
        assertThat(result)
            .isNotNull()
            .isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void convertToEntityAttribute_ShouldThrowForInvalidValue() {
        String invalidStatus = "INVALID_STATUS";
        assertThatThrownBy(() -> converter.convertToEntityAttribute(invalidStatus))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown EventStatus: " + invalidStatus);
    }

    @Test
    void convertToEntityAttribute_ShouldThrowForEmptyString() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown EventStatus: ");
    }

    @Test
    void convertToEntityAttribute_ShouldBeCaseInsensitive() {
        String lowercaseStatus = EventStatus.ACTIVE.name().toLowerCase();
        assertThatThrownBy(() -> converter.convertToEntityAttribute(lowercaseStatus))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown EventStatus: " + lowercaseStatus);
    }

    @Test
    void allEnumValues_ShouldBeHandledBidirectionally() {
        for (EventStatus status : EventStatus.values()) {
            String dbValue = converter.convertToDatabaseColumn(status);
            EventStatus reconverted = converter.convertToEntityAttribute(dbValue);
            assertThat(reconverted)
                .as("Bidirectional conversion should work for %s", status)
                .isEqualTo(status);
        }
    }
}
