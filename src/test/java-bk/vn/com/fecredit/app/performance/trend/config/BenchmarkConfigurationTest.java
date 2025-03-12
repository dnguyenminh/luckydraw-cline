package vn.com.fecredit.app.performance.trend.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class BenchmarkConfigurationTest {

    @Test
    void shouldProvideDefaultArgs() {
        // When
        String[] args = BenchmarkConfiguration.getDefaultArgs();

        // Then
        assertAll(
            () -> assertTrue(Arrays.asList(args).contains("-f"),
                "Should include fork parameter"),
            () -> assertEquals(String.valueOf(BenchmarkConfiguration.DEFAULT_FORKS),
                args[Arrays.asList(args).indexOf("-f") + 1],
                "Should use default fork count"),
            () -> assertTrue(Arrays.asList(args).contains("-wi"),
                "Should include warmup iterations parameter"),
            () -> assertTrue(Arrays.asList(args).contains("-i"),
                "Should include measurement iterations parameter"),
            () -> assertTrue(Arrays.asList(args).contains("-tu"),
                "Should include time unit parameter"),
            () -> assertEquals(BenchmarkConfiguration.DEFAULT_TIME_UNIT.name(),
                args[Arrays.asList(args).indexOf("-tu") + 1],
                "Should use default time unit")
        );
    }

    @Test
    void shouldProvideMemoryProfilingArgs() {
        // When
        String[] args = BenchmarkConfiguration.getMemoryProfilingArgs();

        // Then
        assertAll(
            () -> assertTrue(Arrays.asList(args).contains("-prof"),
                "Should include profiler parameter"),
            () -> assertEquals("gc",
                args[Arrays.asList(args).indexOf("-prof") + 1],
                "Should specify GC profiler"),
            () -> assertEquals("1",
                args[Arrays.asList(args).indexOf("-t") + 1],
                "Should use single thread for memory profiling")
        );
    }

    @Test
    void shouldProvideThroughputArgs() {
        // When
        String[] args = BenchmarkConfiguration.getThroughputArgs();

        // Then
        assertAll(
            () -> assertTrue(Arrays.asList(args).contains("-bm"),
                "Should include benchmark mode parameter"),
            () -> assertEquals("thrpt",
                args[Arrays.asList(args).indexOf("-bm") + 1],
                "Should specify throughput mode"),
            () -> assertEquals(String.valueOf(BenchmarkConfiguration.MAX_THREAD_COUNT),
                args[Arrays.asList(args).indexOf("-t") + 1],
                "Should use maximum thread count for throughput testing")
        );
    }

    @Test
    void shouldProvideLatencyArgs() {
        // When
        String[] args = BenchmarkConfiguration.getLatencyArgs();

        // Then
        assertAll(
            () -> assertTrue(Arrays.asList(args).contains("-bm"),
                "Should include benchmark mode parameter"),
            () -> assertEquals("avgt",
                args[Arrays.asList(args).indexOf("-bm") + 1],
                "Should specify average time mode"),
            () -> assertTrue(Arrays.asList(args).contains("-prof"),
                "Should include profiler parameter"),
            () -> assertEquals("jfr",
                args[Arrays.asList(args).indexOf("-prof") + 1],
                "Should specify JFR profiler")
        );
    }

    @ParameterizedTest(name = "Performance threshold check with {0} ops/sec")
    @ValueSource(doubles = {1_000_000.0, 2_000_000.0, 999_999.0})
    void shouldCheckPerformanceThreshold(double opsPerSec) {
        // When
        boolean meets = BenchmarkConfiguration.meetsPerformanceThreshold(opsPerSec);

        // Then
        assertEquals(opsPerSec >= BenchmarkConfiguration.MIN_OPERATIONS_PER_SECOND, meets,
            "Should correctly evaluate performance threshold");
    }

    @ParameterizedTest(name = "Latency check with {0} microseconds")
    @ValueSource(longs = {50, 100, 150})
    void shouldCheckLatencyThreshold(long latencyMicros) {
        // When
        boolean acceptable = BenchmarkConfiguration.isLatencyAcceptable(latencyMicros);

        // Then
        assertEquals(latencyMicros <= BenchmarkConfiguration.MAX_LATENCY_MICROS, acceptable,
            "Should correctly evaluate latency threshold");
    }

    @ParameterizedTest(name = "Deviation check with {0}%")
    @ValueSource(doubles = {5.0, 10.0, 15.0})
    void shouldCheckDeviationThreshold(double deviation) {
        // When
        boolean acceptable = BenchmarkConfiguration.isDeviationAcceptable(deviation);

        // Then
        assertEquals(deviation <= BenchmarkConfiguration.MAX_DEVIATION_PERCENT, acceptable,
            "Should correctly evaluate deviation threshold");
    }

    @ParameterizedTest(name = "Dataset size for {0}")
    @MethodSource("provideDatasetTypes")
    void shouldProvideCorrectDatasetSize(BenchmarkConfiguration.BenchmarkType type, int expectedSize) {
        // When
        int size = BenchmarkConfiguration.getDatasetSize(type);

        // Then
        assertEquals(expectedSize, size,
            "Should provide correct dataset size for type " + type);
    }

    private static Stream<Arguments> provideDatasetTypes() {
        return Stream.of(
            Arguments.of(BenchmarkConfiguration.BenchmarkType.SMALL,
                BenchmarkConfiguration.SMALL_DATASET_SIZE),
            Arguments.of(BenchmarkConfiguration.BenchmarkType.MEDIUM,
                BenchmarkConfiguration.MEDIUM_DATASET_SIZE),
            Arguments.of(BenchmarkConfiguration.BenchmarkType.LARGE,
                BenchmarkConfiguration.LARGE_DATASET_SIZE)
        );
    }

    @Test
    void shouldHaveReasonableDefaults() {
        assertAll(
            () -> assertTrue(BenchmarkConfiguration.DEFAULT_FORKS > 0,
                "Should have positive fork count"),
            () -> assertTrue(BenchmarkConfiguration.DEFAULT_WARMUP_ITERATIONS > 0,
                "Should have positive warmup iterations"),
            () -> assertTrue(BenchmarkConfiguration.DEFAULT_MEASUREMENT_ITERATIONS > 0,
                "Should have positive measurement iterations"),
            () -> assertTrue(BenchmarkConfiguration.SMALL_DATASET_SIZE < BenchmarkConfiguration.MEDIUM_DATASET_SIZE,
                "Small dataset should be smaller than medium"),
            () -> assertTrue(BenchmarkConfiguration.MEDIUM_DATASET_SIZE < BenchmarkConfiguration.LARGE_DATASET_SIZE,
                "Medium dataset should be smaller than large")
        );
    }

    @Test
    void shouldPreventInstantiation() {
        assertThrows(IllegalAccessException.class, () -> {
            var constructor = BenchmarkConfiguration.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }, "Should prevent instantiation of utility class");
    }
}
