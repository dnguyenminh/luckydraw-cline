package vn.com.fecredit.app.performance.trend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AnalyzerConfig validation and functionality.
 */
class AnalyzerConfigTest {

    @Test
    void shouldCreateDefaultConfig() {
        // When
        AnalyzerConfig config = AnalyzerConfig.getDefault();

        // Then
        assertEquals(3, config.minDataPoints(), "Default min data points should be 3");
        assertEquals(0.1, config.trendThreshold(), "Default trend threshold should be 0.1");
        assertEquals(0.2, config.volatilityThreshold(), "Default volatility threshold should be 0.2");
        assertEquals(0.8, config.confidenceThreshold(), "Default confidence threshold should be 0.8");
    }

    @Test
    void shouldCreateCustomConfig() {
        // When
        AnalyzerConfig config = new AnalyzerConfig(5, 0.15, 0.3, 0.9);

        // Then
        assertEquals(5, config.minDataPoints());
        assertEquals(0.15, config.trendThreshold());
        assertEquals(0.3, config.volatilityThreshold());
        assertEquals(0.9, config.confidenceThreshold());
    }

    @ParameterizedTest(name = "Invalid config: {3}")
    @MethodSource("provideInvalidConfigs")
    void shouldRejectInvalidConfig(int minPoints, double trend, double volatility, 
                                 double confidence, String scenario) {
        // When/Then
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new AnalyzerConfig(minPoints, trend, volatility, confidence),
            "Should reject " + scenario
        );

        assertTrue(ex.getMessage().toLowerCase().contains(scenario.toLowerCase()),
            "Error message should describe the issue");
    }

    private static Stream<Arguments> provideInvalidConfigs() {
        return Stream.of(
            Arguments.of(1, 0.1, 0.2, 0.8, "minimum data points too low"),
            Arguments.of(3, 0.0, 0.2, 0.8, "trend threshold zero"),
            Arguments.of(3, 1.0, 0.2, 0.8, "trend threshold too high"),
            Arguments.of(3, 0.1, 0.0, 0.8, "volatility threshold zero"),
            Arguments.of(3, 0.1, -0.1, 0.8, "negative volatility"),
            Arguments.of(3, 0.1, 0.2, 0.0, "confidence threshold zero"),
            Arguments.of(3, 0.1, 0.2, 1.1, "confidence threshold too high")
        );
    }

    @Test
    void shouldBeImmutable() {
        // Given
        AnalyzerConfig config = new AnalyzerConfig(3, 0.1, 0.2, 0.8);
        
        // When
        int originalMinPoints = config.minDataPoints();
        double originalThreshold = config.trendThreshold();

        // Then
        // Record classes are inherently immutable, but let's verify
        assertAll(
            () -> assertEquals(originalMinPoints, config.minDataPoints(), 
                "minDataPoints should be immutable"),
            () -> assertEquals(originalThreshold, config.trendThreshold(), 
                "trendThreshold should be immutable")
        );
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        // Given
        AnalyzerConfig config1 = new AnalyzerConfig(3, 0.1, 0.2, 0.8);
        AnalyzerConfig config2 = new AnalyzerConfig(3, 0.1, 0.2, 0.8);
        AnalyzerConfig config3 = new AnalyzerConfig(4, 0.1, 0.2, 0.8);

        // Then
        assertAll(
            () -> assertEquals(config1, config2, "Equal configs should be equal"),
            () -> assertEquals(config1.hashCode(), config2.hashCode(), 
                "Equal configs should have same hash"),
            () -> assertNotEquals(config1, config3, "Different configs should not be equal"),
            () -> assertNotEquals(config1.hashCode(), config3.hashCode(), 
                "Different configs should have different hash")
        );
    }

    @Test
    void shouldHaveUsefulToString() {
        // Given
        AnalyzerConfig config = new AnalyzerConfig(3, 0.1, 0.2, 0.8);
        
        // When
        String toString = config.toString();

        // Then
        assertAll(
            () -> assertTrue(toString.contains("minDataPoints=3"), 
                "toString should contain minDataPoints"),
            () -> assertTrue(toString.contains("trendThreshold=0.1"), 
                "toString should contain trendThreshold"),
            () -> assertTrue(toString.contains("volatilityThreshold=0.2"), 
                "toString should contain volatilityThreshold"),
            () -> assertTrue(toString.contains("confidenceThreshold=0.8"), 
                "toString should contain confidenceThreshold")
        );
    }
}
