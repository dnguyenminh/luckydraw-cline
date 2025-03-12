package vn.com.fecredit.app.benchmark;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Runner for JMH benchmarks that provides different execution profiles.
 */
public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        String profile = args.length > 0 ? args[0] : "default";
        Options options = switch (profile.toLowerCase()) {
            case "quick" -> createQuickRunOptions();
            case "thorough" -> createThoroughRunOptions();
            case "profile" -> createProfilingOptions();
            default -> createDefaultOptions();
        };

        new Runner(options).run();
    }

    private static Options createDefaultOptions() {
        return new OptionsBuilder()
            .include(ValidationTestHelperBenchmark.class.getSimpleName())
            .resultFormat(ResultFormatType.JSON)
            .result("benchmark-results.json")
            .shouldDoGC(true)
            .forks(2)
            .warmupIterations(3)
            .measurementIterations(5)
            .timeUnit(TimeUnit.MICROSECONDS)
            .build();
    }

    private static Options createQuickRunOptions() {
        return new OptionsBuilder()
            .include(ValidationTestHelperBenchmark.class.getSimpleName())
            .warmupIterations(1)
            .measurementIterations(2)
            .forks(1)
            .timeUnit(TimeUnit.MICROSECONDS)
            .shouldDoGC(true)
            .build();
    }

    private static Options createThoroughRunOptions() {
        return commonOptionsBuilder()
            .warmupIterations(5)
            .measurementIterations(10)
            .forks(3)
            .warmupTime(TimeValue.seconds(3))
            .measurementTime(TimeValue.seconds(5))
            .addProfiler("gc")
            .build();
    }

    private static Options createProfilingOptions() {
        return commonOptionsBuilder()
            .warmupIterations(3)
            .measurementIterations(5)
            .forks(2)
            .jvmArgsAppend(
                "-XX:+UnlockDiagnosticVMOptions",
                "-XX:+DebugNonSafepoints",
                "-XX:+FlightRecorder"
            )
            .addProfiler("async")
            .addProfiler("gc")
            .addProfiler("stack")
            .resultFormat(ResultFormatType.JSON)
            .result("profiling-results.json")
            .build();
    }

    private static ChainedOptionsBuilder commonOptionsBuilder() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        File resultsDir = new File("benchmark-results");
        resultsDir.mkdirs();

        return new OptionsBuilder()
            .include(ValidationTestHelperBenchmark.class.getSimpleName())
            .resultFormat(ResultFormatType.JSON)
            .result(new File(resultsDir, "benchmark-" + timestamp + ".json").getPath())
            .shouldDoGC(true)
            .timeUnit(TimeUnit.MICROSECONDS)
            .threads(Runtime.getRuntime().availableProcessors())
            .timeout(TimeValue.minutes(10));
    }
}
