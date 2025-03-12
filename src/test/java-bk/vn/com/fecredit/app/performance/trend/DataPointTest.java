package vn.com.fecredit.app.performance.trend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DataPointTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2024, 1, 1, 0, 0);

    @Test
    void shouldCreateValidDataPoint() {
        // When
        DataPoint point = new DataPoint(BASE_TIME, 100.0, 5.0);

        // Then
        assertAll(
            () -> assertEquals(BASE_TIME, point.timestamp()),
            () -> assertEquals(100.0, point.value()),
            () -> assertEquals(5.0, point.deviation())
        );
    }

    @Test
    void shouldRejectNullTimestamp() {
        assertThrows(NullPointerException.class,
            () -> new DataPoint(null, 100.0, 5.0),
            "Should reject null timestamp");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, -0.1})
    void shouldRejectNegativeDeviation(double deviation) {
        assertThrows(IllegalArgumentException.class,
            () -> new DataPoint(BASE_TIME, 100.0, deviation),
            "Should reject negative deviation");
    }

    @ParameterizedTest
    @MethodSource("provideInvalidValues")
    void shouldRejectInvalidValues(double value, double deviation, String description) {
        assertThrows(IllegalArgumentException.class,
            () -> new DataPoint(BASE_TIME, value, deviation),
            "Should reject " + description);
    }

    private static Stream<Arguments> provideInvalidValues() {
        return Stream.of(
            Arguments.of(Double.NaN, 0.0, "NaN value"),
            Arguments.of(Double.POSITIVE_INFINITY, 0.0, "infinite value"),
            Arguments.of(100.0, Double.NaN, "NaN deviation"),
            Arguments.of(100.0, Double.POSITIVE_INFINITY, "infinite deviation")
        );
    }

    @Test
    void shouldCreateWithoutDeviation() {
        // When
        DataPoint point = DataPoint.of(BASE_TIME, 100.0);

        // Then
        assertEquals(0.0, point.deviation(),
            "Should create point with zero deviation");
    }

    @Test
    void shouldCreateWithCurrentTime() {
        // When
        DataPoint point = DataPoint.now(100.0);

        // Then
        assertAll(
            () -> assertNotNull(point.timestamp()),
            () -> assertEquals(100.0, point.value()),
            () -> assertEquals(0.0, point.deviation())
        );
    }

    @Test
    void shouldCalculateTimeDifference() {
        // Given
        DataPoint point1 = new DataPoint(BASE_TIME, 100.0, 0.0);
        DataPoint point2 = new DataPoint(BASE_TIME.plusSeconds(60), 110.0, 0.0);

        // When
        long seconds = point1.secondsFrom(point2);

        // Then
        assertEquals(60, seconds,
            "Should calculate correct time difference");
    }

    @Test
    void shouldRejectNullInTimeDifference() {
        // Given
        DataPoint point = new DataPoint(BASE_TIME, 100.0, 0.0);

        // When/Then
        assertThrows(NullPointerException.class,
            () -> point.secondsFrom(null),
            "Should reject null in secondsFrom");
    }

    @ParameterizedTest
    @MethodSource("provideChangeCalculations")
    void shouldCalculateRelativeChange(double currentValue, double otherValue, double expectedChange) {
        // Given
        DataPoint point = new DataPoint(BASE_TIME, currentValue, 0.0);

        // When
        double change = point.relativeChange(otherValue);

        // Then
        assertEquals(expectedChange, change, 0.0001,
            "Should calculate correct relative change");
    }

    private static Stream<Arguments> provideChangeCalculations() {
        return Stream.of(
            Arguments.of(110.0, 100.0, 0.1),    // 10% increase
            Arguments.of(90.0, 100.0, -0.1),    // 10% decrease
            Arguments.of(100.0, 100.0, 0.0),    // No change
            Arguments.of(0.0, 0.0, 0.0),        // Zero case
            Arguments.of(100.0, 0.0, 1.0),      // Division by zero case
            Arguments.of(-100.0, -100.0, 0.0)   // Negative values
        );
    }

    @Test
    void shouldIdentifyStableMeasurements() {
        assertAll(
            () -> assertTrue(new DataPoint(BASE_TIME, 100.0, 5.0).isStable(),
                "Should be stable when deviation < 10%"),
            () -> assertFalse(new DataPoint(BASE_TIME, 100.0, 15.0).isStable(),
                "Should be unstable when deviation > 10%"),
            () -> assertTrue(new DataPoint(BASE_TIME, 0.0, 0.0).isStable(),
                "Should be stable with zero value and deviation")
        );
    }

    @Test
    void shouldCompareByTimestamp() {
        // Given
        DataPoint earlier = new DataPoint(BASE_TIME, 100.0, 0.0);
        DataPoint later = new DataPoint(BASE_TIME.plusMinutes(1), 110.0, 0.0);

        // Then
        assertTrue(earlier.compareTo(later) < 0,
            "Earlier point should compare less than later point");
        assertTrue(later.compareTo(earlier) > 0,
            "Later point should compare greater than earlier point");
        assertEquals(0, earlier.compareTo(new DataPoint(BASE_TIME, 200.0, 0.0)),
            "Points with same timestamp should be equal");
    }

    @Test
    void shouldProvideUsefulToString() {
        // Given
        DataPoint point = new DataPoint(BASE_TIME, 100.0, 5.0);

        // When
        String result = point.toString();

        // Then
        assertAll(
            () -> assertTrue(result.contains("100.00"),
                "Should include formatted value"),
            () -> assertTrue(result.contains("±5.00"),
                "Should include formatted deviation"),
            () -> assertTrue(result.contains(BASE_TIME.toString()),
                "Should include timestamp")
        );
    }

    @Test
    void shouldCreateModifiedInstances() {
        // Given
        DataPoint original = new DataPoint(BASE_TIME, 100.0, 5.0);

        // When
        DataPoint withNewValue = original.withValue(110.0);
        DataPoint withNewDeviation = original.withDeviation(6.0);

        // Then
        assertAll(
            () -> assertEquals(110.0, withNewValue.value(),
                "Should update value"),
            () -> assertEquals(original.timestamp(), withNewValue.timestamp(),
                "Should preserve timestamp"),
            () -> assertEquals(original.deviation(), withNewValue.deviation(),
                "Should preserve deviation"),
            
            () -> assertEquals(6.0, withNewDeviation.deviation(),
                "Should update deviation"),
            () -> assertEquals(original.timestamp(), withNewDeviation.timestamp(),
                "Should preserve timestamp"),
            () -> assertEquals(original.value(), withNewDeviation.value(),
                "Should preserve value")
        );
    }
}
